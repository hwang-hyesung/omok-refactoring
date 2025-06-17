    package org.omok.newomok.socket;

    import javax.websocket.*;
    import javax.websocket.server.PathParam;
    import javax.websocket.server.ServerEndpoint;
    import java.io.IOException;
    import java.util.*;

    @ServerEndpoint("/game/{gameId}")
    public class GameSocket {
        private static Map<String, Set<Session>> rooms = new HashMap<>();
        private static Map<Session, String> sessionRoomMap = new HashMap<>();
        private static Map<String, List<Session>> roomPlayers = new HashMap<>();
        private static Map<Session, Integer> playerRoles = new HashMap<>(); // 1=흑, 2=백

        // 방별로 15x15 바둑판 상태 관리, 0=빈칸, 1=흑, 2=백
        private static Map<String, int[][]> roomBoards = new HashMap<>();
        private static final int BOARD_SIZE = 15;

        @OnOpen
        public void onOpen(Session session, @PathParam("gameId") String gameId) throws IOException {
            // 일단 방 안 넣고 대기
            String init = String.format("{\"senderId\":\"%s\",\"type\":\"__INIT__\"}", session.getId());
            session.getBasicRemote().sendText(init);
        }

        @OnMessage
        public void onMessage(Session session, String message) throws IOException {
            Map<String, String> msg = parseMessage(message);
            String type = msg.get("type");

            if ("join".equals(type)) {
                String roomId = msg.get("roomId");
                joinRoom(session, roomId);

                // 방 처음 생성 시 바둑판 초기화
                roomBoards.putIfAbsent(roomId, new int[BOARD_SIZE][BOARD_SIZE]);

            } else if ("stone".equals(type)) {
                Integer role = playerRoles.get(session);
                if (role == null) {
                    session.getBasicRemote().sendText("{\"type\":\"error\", \"message\":\"역할이 할당되지 않았습니다.\"}");
                    return;
                }
                int stone = Integer.parseInt(msg.get("stone"));
                if (stone != role) {
                    session.getBasicRemote().sendText("{\"type\":\"error\", \"message\":\"당신 차례가 아닙니다.\"}");
                    return;
                }

                String roomId = sessionRoomMap.get(session);
                int[][] board = roomBoards.get(roomId);

                int row = Integer.parseInt(msg.get("row"));
                int col = Integer.parseInt(msg.get("col"));

                // 이미 돌이 있으면 무시하거나 에러 처리
                if (board[row][col] != 0) {
                    session.getBasicRemote().sendText("{\"type\":\"error\", \"message\":\"이미 돌이 놓여있는 자리입니다.\"}");
                    return;
                }

                // 돌 놓기
                board[row][col] = stone;

                // 돌 놓은 정보 브로드캐스트 (먼저)
                broadcast(roomId, message);
                // 승리 체크
                if (checkWin(board, row, col, stone)) {
                    String gameoverMsg = String.format("{\"type\":\"gameover\", \"winner\":%d}", stone);
                    broadcast(roomId, gameoverMsg);
                    roomBoards.put(roomId, new int[BOARD_SIZE][BOARD_SIZE]);
                }

            } else if ("gameover".equals(type)) {
                // 클라이언트가 보내는 게임오버 메시지 처리 필요 없도록 변경 가능
                String roomId = sessionRoomMap.get(session);
                broadcast(roomId, message);
            }
        }

        private void joinRoom(Session session, String roomId) throws IOException {
            // 이전 방에서 나가기 처리
            String oldRoomId = sessionRoomMap.get(session);
            if (oldRoomId != null && !oldRoomId.equals(roomId)) {
                leaveRoom(session, oldRoomId);
            }

            rooms.putIfAbsent(roomId, Collections.synchronizedSet(new HashSet<>()));
            rooms.get(roomId).add(session);

            roomPlayers.putIfAbsent(roomId, new ArrayList<>());
            List<Session> players = roomPlayers.get(roomId);

            if (!players.contains(session)) {
                if (players.size() < 2) {
                    players.add(session);
                    int role = (players.size() == 1) ? 1 : 2; // 첫 번째 플레이어는 흑돌(1), 두 번째는 백돌(2)
                    playerRoles.put(session, role);

                    String roleName = role == 1 ? "흑" : "백";
                    session.getBasicRemote().sendText(String.format("{\"type\":\"role\", \"role\":%d, \"message\":\"%s돌 플레이어로 입장했습니다.\"}", role, roleName));
                } else {
                    session.getBasicRemote().sendText("{\"type\":\"error\", \"message\":\"이미 플레이어가 2명입니다.\"}");
                    session.close();
                    return;
                }
            }

            // 새 방 입장 시 보드 초기화 (기존 없으면 초기화)
            roomBoards.putIfAbsent(roomId, new int[BOARD_SIZE][BOARD_SIZE]);

            // 새 방 입장 시 클라이언트에 보드 초기화 메시지 전송
            session.getBasicRemote().sendText("{\"type\":\"boardReset\"}");

            if (players.size() == 2) {
                broadcast(roomId, "{\"type\":\"start\", \"message\":\"게임이 시작됩니다.\"}");
            }

            sessionRoomMap.put(session, roomId);
        }

        private void leaveRoom(Session session, String roomId) {
            Set<Session> roomClients = rooms.get(roomId);
            if (roomClients != null) {
                roomClients.remove(session);
                if (roomClients.isEmpty()) {
                    rooms.remove(roomId);
                    roomBoards.remove(roomId);
                    roomPlayers.remove(roomId);
                }
            }

            List<Session> players = roomPlayers.get(roomId);
            if (players != null) {
                players.remove(session);
            }
            playerRoles.remove(session);
            sessionRoomMap.remove(session);
        }

        @OnClose
        public void onClose(Session session) {
            String roomId = sessionRoomMap.get(session);
            if (roomId != null) {
                leaveRoom(session, roomId);
            }
            System.out.println("서버 연결 종료 " + session.getId());
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
            return (countContinuous(board, row, col, 1, 0, stone) + countContinuous(board, row, col, -1, 0, stone) - 1 >= 5) // 가로
                    || (countContinuous(board, row, col, 0, 1, stone) + countContinuous(board, row, col, 0, -1, stone) - 1 >= 5) // 세로
                    || (countContinuous(board, row, col, 1, 1, stone) + countContinuous(board, row, col, -1, -1, stone) - 1 >= 5) // 대각선 \
                    || (countContinuous(board, row, col, 1, -1, stone) + countContinuous(board, row, col, -1, 1, stone) - 1 >= 5); // 대각선 /
        }

        // 연속된 돌 개수 세기 (방향 dx, dy)
        private int countContinuous(int[][] board, int row, int col, int dx, int dy, int stone) {
            int count = 0;
            int x = row;
            int y = col;
            while (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE && board[x][y] == stone) {
                count++;
                x += dx;
                y += dy;
            }
            return count;
        }
    }
