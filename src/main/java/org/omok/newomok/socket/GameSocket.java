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
    // 오목은 2명만 참여하므로 List로 관리 (최대 2개)
    private static final Map<String, List<Session>> rooms = new ConcurrentHashMap<>();
    private static final Map<Session, String> sessionRoomMap = new ConcurrentHashMap<>();
    private static final Map<Session, Integer> playerRoles = new ConcurrentHashMap<>();
    
    // 사용자 ID를 통한 세션 관리 추가
    private static final Map<String, Session> userIdSessionMap = new ConcurrentHashMap<>();
    private static final Map<Session, String> sessionUserIdMap = new ConcurrentHashMap<>();

    // 방별로 15x15 바둑판 상태 관리, 0=빈칸, 1=흑, 2=백
    private static final Map<String, int[][]> roomBoards = new ConcurrentHashMap<>();
    private static final Map<String, Integer> roomTurn = new ConcurrentHashMap<>();
    private static final int BOARD_SIZE = 15;

    @OnOpen
    public void onOpen(Session session, @PathParam("gameId") int gameId) throws IOException {
        String gameIdStr = String.valueOf(gameId);
        
        // 이미 해당 세션이 방에 있는지 확인
        if (sessionRoomMap.containsKey(session)) {
            log.warn("세션이 이미 방에 존재합니다: sessionId={}, gameId={}", session.getId(), gameId);
            return;
        }
        
        // 방에 세션 추가 (최대 2명)
        List<Session> roomSessions = rooms.computeIfAbsent(gameIdStr, k -> Collections.synchronizedList(new ArrayList<>()));
        synchronized (roomSessions) {
            if (roomSessions.size() < 2) {
                roomSessions.add(session);
                sessionRoomMap.put(session, gameIdStr);
            } else {
                log.warn("방이 가득 찼습니다: gameId={}", gameId);
                session.close();
                return;
            }
        }

        String init = String.format("{\"senderId\":\"%s\",\"type\":\"INIT\"}", session.getId());
        session.getBasicRemote().sendText(init);

        log.info("새 게임 소켓 연결: sessionID={}, gameId={}", session.getId(), gameId);
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
            String userId = msg.get("userId");

            // 사용자 ID와 세션 매핑
            if (userId != null) {
                // 기존 세션이 있다면 정리 (같은 사용자의 세션만)
                Session existingSession = userIdSessionMap.get(userId);
                if (existingSession != null && !existingSession.equals(session)) {
                    log.info("기존 세션 정리: userId={}, oldSessionId={}", userId, existingSession.getId());
                    // 기존 세션이 열려있으면 닫기
                    if (existingSession.isOpen()) {
                        try {
                            existingSession.close();
                        } catch (Exception e) {
                            log.warn("기존 세션 닫기 실패: {}", e.getMessage());
                        }
                    }
                    // 기존 세션 정보 정리
                    leaveRoom(existingSession, gameIdStr);
                }
                
                userIdSessionMap.put(userId, session);
                sessionUserIdMap.put(session, userId);
            }

            // 역할 설정 및 방 초기화
            playerRoles.put(session, role);
            
            // 게임 보드와 턴 초기화 (이미 존재하면 유지)
            if (!roomBoards.containsKey(gameIdStr)) {
                roomBoards.put(gameIdStr, new int[BOARD_SIZE][BOARD_SIZE]);
                roomTurn.put(gameIdStr, 1);
                log.info("새 게임 시작: gameId={}", gameId);
            } else {
                log.info("기존 게임 복원: gameId={}, 현재 턴={}", gameId, roomTurn.get(gameIdStr));
            }

            // 클라이언트에 보드 초기화 메시지 전송
            int[][] board = roomBoards.get(gameIdStr);
            int turn = roomTurn.get(gameIdStr);

            // 보드 배열 직렬화
            StringBuilder boardJson = new StringBuilder("[");
            for (int i = 0; i < BOARD_SIZE; i++) {
                boardJson.append("[");
                for (int j = 0; j < BOARD_SIZE; j++) {
                    boardJson.append(board[i][j]);
                    if (j < BOARD_SIZE - 1) boardJson.append(",");
                }
                boardJson.append("]");
                if (i < BOARD_SIZE - 1) boardJson.append(",");
            }
            boardJson.append("]");

            try {
                session.getBasicRemote().sendText(String.format("{\"type\":\"role\", \"role\":%d}", role));
                session.getBasicRemote().sendText(String.format("{\"type\":\"STATE\", \"turn\":%d, \"board\":%s}", turn, boardJson));
            } catch (Exception e) {
                log.warn("메시지 전송 실패: sessionId={}, error={}", session.getId(), e.getMessage());
            }
            
            log.info("게임 참가: sessionID={}, gameId={}, role={}, userId={}", session.getId(), gameId, role, userId);
        } else if ("STONE".equals(type)) {
            //게임 진행 중 메시지
            log.debug("게임 진행: sessionID={}, gameId={}, role={}", session.getId(), gameId, playerRoles.get(session));
            
            //해당 게임의 게임판 불러오기
            int[][] board = roomBoards.get(gameIdStr);

            synchronized (board) {
                int row = Integer.parseInt(msg.get("row"));
                int col = Integer.parseInt(msg.get("col"));
                int stone = Integer.parseInt(msg.get("stone"));

                //턴 확인
                int currentTurn = roomTurn.get(gameIdStr);
                if (stone != currentTurn) {
                    try {
                        session.getBasicRemote().sendText("{\"type\":\"ERROR\", \"message\":\"지금은 상대방의 차례입니다.\"}");
                    } catch (Exception e) {
                        log.warn("에러 메시지 전송 실패: {}", e.getMessage());
                    }
                    return;
                }

                // 이미 돌이 있거나 순서가 아니면 무시하거나 에러 처리
                if (board[row][col] != 0 ) {
                    try {
                        session.getBasicRemote().sendText("{\"type\":\"ERROR\", \"message\":\"이미 돌이 놓여있는 자리입니다.\"}");
                    } catch (Exception e) {
                        log.warn("에러 메시지 전송 실패: {}", e.getMessage());
                    }
                    return;
                }

                // 돌 놓기
                board[row][col] = stone;
                log.info("돌 놓기: gameId={}, row={}, col={}, stone={}", gameId, row, col, stone);

                // 돌 놓은 정보 브로드캐스트 (먼저)
                broadcast(gameIdStr, message);

                // 승리 체크
                if (checkWin(board, row, col, stone)) {
                    String gameoverMsg = String.format("{\"type\":\"GAMEOVER\", \"winner\":%d}", stone);
                    broadcast(gameIdStr, gameoverMsg);
                    
                    // 게임 종료 시 보드 초기화
                    roomBoards.put(gameIdStr, new int[BOARD_SIZE][BOARD_SIZE]);
                    roomTurn.put(gameIdStr, 1);
                    log.info("게임 종료: gameId={}, 승자={}", gameId, stone);
                } else {
                    // 턴 전환
                    roomTurn.put(gameIdStr, 3 - stone);
                    log.info("턴 전환: gameId={}, 다음 턴={}", gameId, 3 - stone);
                }
            }
        }
    }

    private void leaveRoom(Session session, String roomId) {
        List<Session> roomSessions = rooms.get(roomId);
        if (roomSessions != null) {
            synchronized (roomSessions) {
                roomSessions.remove(session);
                if (roomSessions.isEmpty()) {
                    rooms.remove(roomId);
                    // 게임이 완전히 끝났을 때만 보드와 턴 정보 제거
                    // roomBoards.remove(roomId);
                    // roomTurn.remove(roomId);
                    log.info("방 비워짐 → roomId={} 제거 (보드 상태 유지)", roomId);
                }
            }
        }

        // 세션 관련 정보 정리
        playerRoles.remove(session);
        sessionRoomMap.remove(session);
        
        // 사용자 ID 관련 정보 정리
        String userId = sessionUserIdMap.remove(session);
        if (userId != null) {
            userIdSessionMap.remove(userId);
        }
    }

    private void broadcast(String roomId, String payload) throws IOException {
        List<Session> roomSessions = rooms.get(roomId);
        if (roomSessions == null) return;

        synchronized (roomSessions) {
            for (Session s : roomSessions) {
                if (s.isOpen()) {
                    try {
                        s.getBasicRemote().sendText(payload);
                    } catch (Exception e) {
                        log.warn("브로드캐스트 메시지 전송 실패: sessionId={}, error={}", s.getId(), e.getMessage());
                    }
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
        
        // 세션이 실제로 방에 있는지 확인 후 정리
        if (sessionRoomMap.containsKey(session)) {
            leaveRoom(session, gameIdStr);
            log.info("게임 소켓 연결 종료: sessionID={}, gameId={}", session.getId(), gameId);
        } else {
            log.warn("정리할 세션이 방에 없습니다: sessionID={}, gameId={}", session.getId(), gameId);
        }
    }
}


