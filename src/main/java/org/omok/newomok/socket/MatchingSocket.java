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
import org.omok.newomok.util.JsonBuilderUtil;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@ServerEndpoint(value = "/matching")
public class MatchingSocket {
    // gameId → 세션들
    // Set<Session> -> 해당 게임방에 들어온 유저들
    private static final Map<Integer, Set<Session>> gameRoomMap = new ConcurrentHashMap<>();
    // 세션 → gameId
    private static final Map<Session, Integer> sessionRoomMap = new ConcurrentHashMap<>();
    
    // 게임이 시작된 방들을 추적 (매칭 소켓이 닫혀도 게임은 계속 진행)
    private static final Set<Integer> activeGames = new HashSet<>();

    private static final MatchDAO matchDAO = MatchDAO.INSTANCE;
    private static final GameDAO gameDAO = GameDAO.INSTANCE;
    private static final UserDAO userDAO = UserDAO.INSTANCE;

    @OnOpen
    public void onOpen(Session session) throws IOException {
        //1. 엔드포인트에서 gameId 파싱
        int gameId = Integer.parseInt(session.getRequestParameterMap().get("gameId").get(0));

        //2. 방에 세션 추가
        Set<Session> set = gameRoomMap.computeIfAbsent(gameId, k -> Collections.synchronizedSet(new HashSet<>()));
        synchronized (set) {
            set.add(session);
        }

        sessionRoomMap.put(session, gameId);

        // 접속 인원 수 확인
        if(gameRoomMap.get(gameId).size() == 1) {
            System.out.println("matching socket - WAITING");

            // 1명: 대기 상태 메시지 전송
            JsonObject response = new JsonObject();
            response.addProperty("status", "WAITING");
            response.addProperty("gameId", gameId);

            session.getBasicRemote().sendText(response.toString());

        } else {
            System.out.println("matching socket - MATCHED");

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

            Set<Session> sessions = gameRoomMap.get(gameId);

            for (Session s : sessions) {
                s.getBasicRemote().sendText(response.toString());
            }
            
            // 게임이 시작되었음을 표시
            activeGames.add(gameId);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        //게임이 끝나면 소켓 닫기 (게임 소켓, 챗 소켓)
    }


    @OnClose
    public void onClose(Session session) {
        Integer gameId = sessionRoomMap.remove(session);
        if (gameId != null) {
            Set<Session> sessions = gameRoomMap.get(gameId);
            if (sessions != null) {
                sessions.remove(session);

                // 게임이 활성 상태가 아니고, 상대방이 남아 있다면만 게임오버 처리
                if (!activeGames.contains(gameId) && !sessions.isEmpty()) {
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
                    activeGames.remove(gameId);
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