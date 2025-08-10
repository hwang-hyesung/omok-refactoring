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
import java.util.*;
import java.util.concurrent.*;

@Log4j2
@ServerEndpoint(value = "/matching")
public class MatchingSocket {
    // gameId → 세션들
    // Set<Session> -> 해당 게임방에 들어온 유저들
    private static final Map<Integer, Set<Session>> gameRoomMap = new ConcurrentHashMap<>();
    // 세션 → gameId
    private static final Map<Session, Integer> sessionRoomMap = new ConcurrentHashMap<>();

    //재접속하는 유저 id
    private static final Map<Integer, Integer> reconnectedUsers = new ConcurrentHashMap<>(); //gameId, userId

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
        Set<Session> set = gameRoomMap.computeIfAbsent(gameId, k -> Collections.synchronizedSet(new HashSet<>()));
        synchronized (set) {
            set.add(session);
        }

        sessionRoomMap.put(session, gameId);

        // 접속 인원 수 확인
        if(gameRoomMap.get(gameId).size() == 1) {
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

            Set<Session> sessions = gameRoomMap.get(gameId);

            for (Session s : sessions) {
                s.getBasicRemote().sendText(response.toString());
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        //Reconnect 처리
        int gameId = Integer.parseInt(session.getRequestParameterMap().get("gameId").get(0));

        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        int userId = jsonObject.get("userId").getAsInt();

        if("RECONNECT".equals(type)) {
            reconnectedUsers.put(gameId, userId);
        }
    }


    @OnClose
    public void onClose(Session session) {
        int gameId = Integer.parseInt(session.getRequestParameterMap().get("gameId").get(0));
        int currentSize = gameRoomMap.get(gameId).size();
        if(currentSize == 1) {
            //매칭 중 나간 경우
            scheduler.schedule(() -> {
//                if (!reconnectedUsers.contains(userId)) {
//                    // 진짜 나감 처리
//                }
            }, 3, TimeUnit.SECONDS);

            if(!matchDAO.deleteGameById(gameId)) {
                log.error("matching socket - CLOSED: 1명 삭제 중 오류 발생");
            }

        }
        log.info("matching socket 연결 종료 - gameId: {}", gameId);
    }


    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("matching socket 에러 발생:", throwable);
    }

}