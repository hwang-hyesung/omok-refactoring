package org.omok.newomok.socket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.log4j.Log4j2;
import org.omok.newomok.domain.GameVO;
import org.omok.newomok.domain.UserVO;
import org.omok.newomok.repository.GameDAO;
import org.omok.newomok.repository.MatchDAO;
import org.omok.newomok.repository.UserDAO;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
@ServerEndpoint(value = "/matching")
public class MatchingSocket {
    // gameId → 세션들
    // Set<Session> -> 해당 게임방에 들어온 유저들
    private static final Map<Integer, Map<String, Session>> gameRoomMap = new ConcurrentHashMap<>(); //gameId, user(userId, Session)
    // 세션 → gameId
    private static final Map<Session, Integer> sessionRoomMap = new ConcurrentHashMap<>();

    // 게임이 시작된 방들을 추적 (매칭 소켓이 닫혀도 게임은 계속 진행)
    private static final Set<Integer> activeGames = new HashSet<>();

    // 해당 방에 접속한 아이디(재접속 감지용)
    private static final Map<Integer, Map<String, Session>> reconnectedUsers = new ConcurrentHashMap<>(); //gameId, user(userId, Session)

    ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(4);


    private static final MatchDAO matchDAO = MatchDAO.INSTANCE;
    private static final GameDAO gameDAO = GameDAO.INSTANCE;
    private static final UserDAO userDAO = UserDAO.INSTANCE;

    @OnOpen
    public void onOpen(Session session) throws IOException {
        log.info("matching socket - open: session - " + session.getId());
        //1. 엔드포인트에서 gameId 파싱
        int gameId = Integer.parseInt(session.getRequestParameterMap().get("gameId").get(0));

        //2. 방에 세션 추가
        sessionRoomMap.put(session, gameId);
        gameRoomMap.computeIfAbsent(gameId, k-> new ConcurrentHashMap<>());

    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        int gameId = Integer.parseInt(session.getRequestParameterMap().get("gameId").get(0));

        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();

        String type = jsonObject.get("type").getAsString();
        String userId = jsonObject.get("id").getAsString();

        if("CONNECT".equals(type)) {
            //최초 접속
            Map<String, Session> roomMap = gameRoomMap.get(gameId);
            roomMap.put(userId, session);

            // 접속 인원 수 확인
            if(roomMap.size() == 1) {
                log.info("matching socket - WAITING");

                // 1명: 대기 상태 메시지 전송
                JsonObject response = new JsonObject();
                response.addProperty("status", "WAITING");
                response.addProperty("gameId", gameId);

                session.getBasicRemote().sendText(response.toString());

            } else {
                log.info("matching socket - MATCHED");

                // 2명: 매칭 상태 메시지 전송
                JsonObject response = new JsonObject();
                response.addProperty("status", "MATCHED");
                response.addProperty("gameId", gameId);

                // 해당 gameId로 게임 정보 가져오기
                GameVO game = MatchDAO.getGameById(gameId);
                UserVO player1 = userDAO.getUserById(game.getPlayer1());
                UserVO player2 = userDAO.getUserById(game.getPlayer2());

                Gson gson = new Gson();
                response.addProperty("player1", gson.toJsonTree(player1).toString());
                response.addProperty("player2", gson.toJsonTree(player2).toString());

                Map<String, Session> sessions = gameRoomMap.get(gameId);

                for (String s : sessions.keySet()) {
                    sessions.get(s).getBasicRemote().sendText(response.toString());
                }

                // 게임이 시작되었음을 표시
                activeGames.add(gameId);
            }

        } else if("RECONNECT".equals(type)) {
            reconnectedUsers
                    .computeIfAbsent(gameId, k-> new ConcurrentHashMap<>())
                    .put(userId, session);

            Session prevSession = gameRoomMap.get(gameId).get(userId);


            gameRoomMap.get(gameId).remove(userId);
            gameRoomMap.get(gameId).put(userId, session);

            // 세션 → gameId
            sessionRoomMap.remove(prevSession);
            sessionRoomMap.put(session, gameId);
        }
    }


    @OnClose
    public void onClose(Session session) {
        int gameId = Integer.parseInt(session.getRequestParameterMap().get("gameId").get(0));

        scheduler.schedule(() -> {
            Map<String, Session> userMap = reconnectedUsers.get(gameId);
            if(userMap != null) {
                userMap.values().removeIf(s -> s.equals(session));
                if(userMap.isEmpty()) {
                    reconnectedUsers.remove(gameId);
                }
            } else {
                sessionRoomMap.remove(session);
                Map<String, Session> sessions = gameRoomMap.get(gameId);
                if (sessions != null) {
                    sessions.remove(session);

                    if(gameRoomMap.size() == 1) {
                        matchDAO.deleteGameById(gameId);
                    }

                    // 게임이 활성 상태가 아니고, 상대방이 남아 있다면만 게임오버 처리
                    if (!activeGames.contains(gameId) && !sessions.isEmpty()) {
                        try {
                            // 남아있는 사람에게 승리 메시지 전달
                            String winnerId = sessions.keySet().iterator().next();
                            Session remainingSession = sessions.get(winnerId);

                            JsonObject gameoverMsg = new JsonObject();
                            gameoverMsg.addProperty("type", "gameover");
                            gameoverMsg.addProperty("winnerId", winnerId);
                            remainingSession.getBasicRemote().sendText(gameoverMsg.toString());
                        } catch (IOException e) {
                            log.error("게임 종료 메시지 전송 실패", e);
                        }
                    }

                    if (sessions.isEmpty()) {
                        gameRoomMap.remove(gameId);
                    }
                }
            }
        }, 2, TimeUnit.SECONDS);


        log.info("[WebSocket] 연결 종료 - gameId: {}", gameId);
    }


    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("[WebSocket] 에러 발생:", throwable);
    }

}