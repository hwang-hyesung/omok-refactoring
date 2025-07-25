package org.omok.newomok.socket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/chat/{gameId}")
public class ChatSocket {

    // 게임 ID 별로 세션을 관리하기 위함
    private static Map<String, Set<Session>> chatRooms = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("gameId") String gameId) throws IOException {
        // gameId 방이 없으면 새로 만들고, 세션 추가
        chatRooms.putIfAbsent(gameId, new HashSet<>());
        chatRooms.get(gameId).add(session);
        session.getUserProperties().put("gameId", gameId);

        // 접속자에게 초기 메시지 전달
        String init = String.format("{\"senderId\":\"%s\",\"status\":\"INIT\"}", session.getId());
        session.getBasicRemote().sendText(init);

        System.out.println("new connection - chat socket: sessionID=" + session.getId() + ", game room=" + gameId);
    }

    @OnMessage
    public void onMessage(Session session, String message, @PathParam("gameId") String gameId) throws IOException {
        System.out.println("chat socket - message: sessionID=" + session.getId() + ", game room=" + gameId);

        if (gameId == null) return;

        Set<Session> room = chatRooms.get(gameId);
        if (room == null) return;

        // JSON 파싱
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(message, JsonObject.class);

        String senderId = json.has("senderId") ? json.get("senderId").getAsString() : session.getId();
        String text = json.has("message") ? json.get("message").getAsString() : "";

        // JSON escape
        String escaped = text
                .replaceAll("\\\\", "\\\\\\\\")
                .replaceAll("\"", "\\\\\"")
                .replaceAll("\n", "\\\\n");

        // 전송할 payload 생성
        String payload = String.format(
                "{\"senderId\":\"%s\",\"text\":\"%s\",\"status\":\"CHAT\"}", senderId, escaped
        );

        synchronized (room) {
            for (Session s : room) {
                if (s.isOpen()) {
                    s.getBasicRemote().sendText(payload);
                }
            }
        }
    }


    @OnClose
    public void onClose(Session session, @PathParam("gameId") String gameId) {
        if (gameId != null) {
            Set<Session> room = chatRooms.get(gameId);
            if (room != null) {
                room.remove(session);
                if (room.isEmpty()) {
                    // 방이 비면 맵에서 제거
                    chatRooms.remove(gameId);
                    System.out.println("게임방 " + gameId + "이 비어서 제거됨");
                }
            }
        }

        System.out.println("chat socket - connection end: sessionID=" + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable thr, @PathParam("gameId") String gameId) {
        System.err.println("chat socket - error: sessionID=" + (session != null ? session.getId() : "null"));
        thr.printStackTrace();
    }
}
