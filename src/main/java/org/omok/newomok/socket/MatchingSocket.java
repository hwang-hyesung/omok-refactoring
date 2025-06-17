package org.omok.newomok.socket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.log4j.Log4j2;
import org.omok.newomok.domain.GameVO;
import org.omok.newomok.domain.UserVO;
import org.omok.newomok.repository.GameDAO;
import org.omok.newomok.repository.MatchDAO;
import org.omok.newomok.repository.UserDAO;
import org.omok.newomok.util.JsonBuilderUtil;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@ServerEndpoint(value = "/min-value")
public class MatchingSocket {
    // gameId → 세션들
    // Set<Session>가 해당 게임방에 들어온 유저들이다.
    private static final Map<Integer, Set<Session>> gameRoomMap = new ConcurrentHashMap<>();
    // 세션 → gameId
    private static final Map<Session, Integer> sessionRoomMap = new ConcurrentHashMap<>();

    private static final MatchDAO matchDAO = MatchDAO.INSTANCE;
    private static final GameDAO gameDAO = GameDAO.INSTANCE;
    private static final UserDAO userDAO = UserDAO.INSTANCE;

    private Session session;
    private int gameId;

    // 소켓의 onOpen은 클라이언트가 연결될때 호출되는 것으로, 이 부분은 무조건 매칭만 사용한다.
    @OnOpen
    public void onOpen(Session session) throws IOException {
        this.session = session;
        String query = session.getQueryString();
        String userId = query.split("=")[1]; // userId 파라미터로 변경
        session.getUserProperties().put("userId", userId); //세션에 user id 저장

        // 새로운 게임 생성
        GameVO game = GameVO.builder()
                .status(GameVO.GameStatus.WAITING)
                .player1(userId)
                .build();
        game = MatchDAO.makeGame(game);
        this.gameId = game.getGameId();

        // 방에 세션 추가
        gameRoomMap.computeIfAbsent(gameId, k -> ConcurrentHashMap.newKeySet()).add(session);
        sessionRoomMap.put(session, gameId);

        // 대기 상태 메시지 전송
        JsonObject response = new JsonObject();
        response.addProperty("status", "WAITING");
        response.addProperty("gameId", gameId);
        session.getBasicRemote().sendText(response.toString());
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        JsonObject receivedJson = JsonParser.parseString(message).getAsJsonObject();
        String type = receivedJson.get("type").getAsString();

        if ("JOIN".equals(type)) {
            String userId = receivedJson.get("userId").getAsString();
            int gameId = receivedJson.get("gameId").getAsInt();

            GameVO game = MatchDAO.getGameById(gameId);
            if (game != null && game.getStatus() == GameVO.GameStatus.WAITING) {
                game.setPlayer2(userId);
                game.setStatus(GameVO.GameStatus.PLAYING);
                matchDAO.updateGame(game);

                // 두 세션 모두에게 MATCHED 메시지 전송
                Set<Session> sessions = gameRoomMap.get(gameId);
                if (sessions != null) {
                    sendMatchedMessageToBoth(gameId, sessions);
                }
            }
        }
    }

    // 소켓 실시간 양방향 매칭
    private void sendMatchedMessageToBoth(int gameId, Set<Session> sessions) throws IOException {

        GameVO game = MatchDAO.getGameById(gameId);
        game.setStatus(GameVO.GameStatus.PLAYING);
        matchDAO.updateGame(game);

        UserVO player1 = userDAO.getUserById(game.getPlayer1());
        UserVO player2 = userDAO.getUserById(game.getPlayer2());

        for (Session s : sessions) {
            if (!s.isOpen()) continue;

            String userId = (String) s.getUserProperties().get("userId"); //Session에서 userid 가져오기

            UserVO you = userId.equals(player1.getUserId()) ? player1 : player2;
            UserVO opponent = userId.equals(player1.getUserId()) ? player2 : player1;

            JsonObject response = new JsonObject();
            response.addProperty("status", "MATCHED");
            response.add("you", JsonBuilderUtil.getUserInfo(you));
            response.add("opponent", JsonBuilderUtil.getUserInfo(opponent));
            response.add("game", JsonBuilderUtil.getGameInfo(game));

            s.getBasicRemote().sendText(response.toString());
        }
    }

    @OnClose
    public void onClose(Session session) {
        Integer gameId = sessionRoomMap.remove(session);
        if (gameId != null) {
            Set<Session> sessions = gameRoomMap.get(gameId);
            if (sessions != null) {
                sessions.remove(session);

                // 상대방이 남아 있다면, 그 사람에게 승리 메시지 전달
                if (!sessions.isEmpty()) {
                    try {
                        // 남아있는 사람에게 승리 메시지 전달
                        Session remainingSession = sessions.iterator().next();
                        String winnerId = (String) remainingSession.getUserProperties().get("userId");

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
        log.info("[WebSocket] 연결 종료 - gameId: {}", gameId);
    }


    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("[WebSocket] 에러 발생:", throwable);
    }

}