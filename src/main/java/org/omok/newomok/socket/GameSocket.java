package org.omok.newomok.socket;

import lombok.extern.slf4j.Slf4j;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint("/game/{gameId}")
public class GameSocket {
    private static final Map<String, Set<Session>> rooms = new ConcurrentHashMap<>();
    private static final Map<Session, String> sessionRoomMap = new ConcurrentHashMap<>();
    private static final Map<Session, Integer> playerRoles = new ConcurrentHashMap<>();

    // 방별로 15x15 바둑판 상태 관리, 0=빈칸, 1=흑, 2=백
    private static final Map<String, int[][]> roomBoards = new ConcurrentHashMap<>();
    private static final Map<String, Integer> roomTurn = new ConcurrentHashMap<>();
    private static final int BOARD_SIZE = 15;


    @OnOpen
    public void onOpen(Session session, @PathParam("gameId") int gameId) throws IOException {
//
//        // 일단 방 안 넣고 대기
//        String init = String.format("{\"senderId\":\"%s\",\"type\":\"INIT\"}", session.getId());
//        session.getBasicRemote().sendText(init);
//        System.out.println("new connection - game socket: sessionID=" + session.getId() + ", game room=" + gameId);
        Set<Session> set = rooms.computeIfAbsent(String.valueOf(gameId), k -> Collections.synchronizedSet(new HashSet<>()));
        synchronized (set) {
            set.add(session);
        }

        String init = String.format("{\"senderId\":\"%s\",\"type\":\"INIT\"}", session.getId());
        session.getBasicRemote().sendText(init);

        System.out.println("new connection - game socket: sessionID=" + session.getId() + ", game room=" + gameId);
    }

    @OnMessage
    public void onMessage(Session session, @PathParam("gameId") int gameId, String message) throws IOException {
        Map<String, String> msg = parseMessage(message);
        String type = msg.get("type");
        String gameIdStr = String.valueOf(gameId);

        if ("JOIN".equals(type)) {
            //게임 매칭 후 최초 메시지

            //메시지 보낸 사람 역할
            int role = Integer.parseInt(msg.get("role"));

            //방에 참가
            joinRoom(session, gameIdStr, role);
        } else if ("STONE".equals(type)) {
            //게임 진행 중 메시지
            System.out.println("game socket - playing: sessionID=" + session.getId() + ", game room=" + gameId + ", role=" + playerRoles.get(session));

            //해당 게임의 게임판 불러오기
            int[][] board = roomBoards.get(gameIdStr);

            synchronized (board) {
                int row = Integer.parseInt(msg.get("row"));
                int col = Integer.parseInt(msg.get("col"));
                int stone = Integer.parseInt(msg.get("stone"));

                //턴 확인
                int currentTurn = roomTurn.get(gameIdStr);
                if (stone != currentTurn) {
                    session.getBasicRemote().sendText("{\"type\":\"ERROR\", \"message\":\"지금은 상대방의 차례입니다.\"}");
                    return;
                }

                // 이미 돌이 있거나 순서가 아니면 무시하거나 에러 처리
                if (board[row][col] != 0 ) {
                    session.getBasicRemote().sendText("{\"type\":\"ERROR\", \"message\":\"이미 돌이 놓여있는 자리입니다.\"}");
                    return;
                }

                // 돌 놓기
                board[row][col] = stone;

                // 돌 놓은 정보 브로드캐스트 (먼저)
                broadcast(gameIdStr, message);

                // 승리 체크
                if (checkWin(board, row, col, stone)) {
                    String gameoverMsg = String.format("{\"type\":\"GAMEOVER\", \"winner\":%d}", stone);
                    broadcast(gameIdStr, gameoverMsg);
                    roomBoards.put(gameIdStr, new int[BOARD_SIZE][BOARD_SIZE]);
                } else {
                    // 턴 전환
                    roomTurn.put(gameIdStr, 3 - stone);
                }
            }

        }
    }

    private void joinRoom(Session session, String roomId, int role) throws IOException {
        Set<Session> set = rooms.computeIfAbsent(roomId, k -> Collections.synchronizedSet(new HashSet<>()));
        synchronized (set) {
            set.add(session);
        }

        // 새 방 입장 시 보드 초기화 (기존 없으면 초기화)
        roomBoards.putIfAbsent(roomId, new int[BOARD_SIZE][BOARD_SIZE]);
        playerRoles.put(session, role);
        sessionRoomMap.put(session, roomId);
        roomTurn.putIfAbsent(String.valueOf(roomId), 1);

        // 새 방 입장 시 클라이언트에 보드 초기화 메시지 전송
        session.getBasicRemote().sendText(String.format("{\"type\":\"role\", \"role\":%d}", role));
    }

    private void leaveRoom(Session session, String roomId) {
        Set<Session> roomClients = rooms.get(roomId);
        if (roomClients != null) {
            synchronized (roomClients) {
                roomClients.remove(session);
                if (roomClients.isEmpty()) {
                    rooms.remove(roomId);
                    roomBoards.remove(roomId);
                }
            }
        }

        Set<Session> players = rooms.get(roomId);

        if (players != null) {
            players.remove(session);
        }

        playerRoles.remove(session);
        sessionRoomMap.remove(session);
        roomTurn.remove(roomId);
    }

    private void broadcast(String roomId, String payload) throws IOException {
        Set<Session> roomClients = rooms.get(roomId);
        if (roomClients == null) return;

        synchronized (roomClients) {
            for (Session s : roomClients) {
                if (s.isOpen()) {
                    s.getBasicRemote().sendText(payload);
                }
            }
        }
    }

    private Map<String, String> parseMessage(String message) {
        Map<String, String> map = new HashMap<>();
        message = message.trim();
        if (message.startsWith("{")) message = message.substring(1);
        if (message.endsWith("}")) message = message.substring(0, message.length() - 1);

        String[] pairs = message.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replace("\"", "");
                String value = kv[1].trim().replace("\"", "");
                map.put(key, value);
            }
        }
        return map;
    }

    // 5목 연속인지 체크하는 함수 (수평, 수직, 대각선 검사)
    private boolean checkWin(int[][] board, int row, int col, int stone) {
        return (count(board, row, col, 1, 0, stone) + count(board, row, col, -1, 0, stone) - 1 >= 5) ||
                (count(board, row, col, 0, 1, stone) + count(board, row, col, 0, -1, stone) - 1 >= 5) ||
                (count(board, row, col, 1, 1, stone) + count(board, row, col, -1, -1, stone) - 1 >= 5) ||
                (count(board, row, col, 1, -1, stone) + count(board, row, col, -1, 1, stone) - 1 >= 5);
    }

    // 연속된 돌 개수 세기 (방향 dx, dy)
    private int count(int[][] board, int x, int y, int dx, int dy, int stone) {
        int cnt = 0;
        while (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE && board[x][y] == stone) {
            cnt++; x += dx; y += dy;
        }
        return cnt;
    }

    @OnClose
    public void onClose(@PathParam("gameId") int gameId, Session session) {
        String gameIdStr = String.valueOf(gameId);
        leaveRoom(session, gameIdStr);
        System.out.println("game socket - connection end: sessionID=" + session.getId());
    }
}
