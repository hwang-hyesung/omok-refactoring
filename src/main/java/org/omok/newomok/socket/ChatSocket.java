package org.omok.newomok.socket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/chat/{gameId}")
public class ChatSocket {

    // gameId -> (userId -> Session)
    private static final Map<String, Map<String, Session>> chatRooms = new ConcurrentHashMap<>();

    private static String extractUserId(Session session) {
        try {
            Map<String, List<String>> map = session.getRequestParameterMap();
            if (map != null && map.containsKey("userId") && !map.get("userId").isEmpty()) {
                return map.get("userId").get(0);
            }
        } catch (Exception ignore) {}
        return session.getId();
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("gameId") String gameId) throws IOException {
        if (gameId == null) {
            safeClose(session, "gameId-null");
            return;
        }

        String userId = extractUserId(session);

        chatRooms.computeIfAbsent(gameId, k -> new ConcurrentHashMap<>());
        Map<String, Session> room = chatRooms.get(gameId);

        // 동일 userId 기존 세션이 있으면 교체
        Session old = room.put(userId, session);
        if (old != null && old.isOpen() && old != session) {
            safeClose(old, "replaced");
        }

        session.getUserProperties().put("gameId", gameId);
        session.getUserProperties().put("userId", userId);

        // 초기 메시지: userId로 통일
        String init = String.format("{\"senderId\":\"%s\",\"status\":\"INIT\"}", escape(userId));
        session.getBasicRemote().sendText(init);

        System.out.println("chat open: game=" + gameId + ", user=" + userId + ", sid=" + session.getId());
    }

    @OnMessage
    public void onMessage(Session session, String raw, @PathParam("gameId") String gameId) {
        if (session == null || gameId == null) return;

        Map<String, Session> room = chatRooms.get(gameId);
        if (room == null) {
            // 방이 없으면 무시 (선택적으로 생성해도 됨)
            return;
        }

        String userId = String.valueOf(session.getUserProperties().getOrDefault("userId", session.getId()));

        // JSON 파싱
        String text = "";
        try {
            JsonObject json = JsonParser.parseString(raw).getAsJsonObject();
            if (json.has("message") && !json.get("message").isJsonNull()) {
                text = json.get("message").getAsString();
            }
        } catch (Exception e) {
            return;
        }

        String payload = "{\"senderId\":\"" + escape(userId) + "\",\"text\":\"" + escape(text) + "\",\"status\":\"CHAT\"}";

        // 좀비 세션 청소
        room.values().removeIf(s -> s == null || !s.isOpen());

        // 브로드캐스트
        for (Session s : room.values()) {
            try {
                s.getBasicRemote().sendText(payload);
            } catch (Exception e) {
                safeClose(s, "send-fail");
            }
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("gameId") String gameId) {
        cleanup(session, gameId, "close");
    }

    @OnError
    public void onError(Session session, Throwable thr, @PathParam("gameId") String gameId) {
        cleanup(session, gameId, "error");
        if (thr != null) thr.printStackTrace();
    }

    private static void cleanup(Session session, String gameId, String reason) {
        if (session == null || gameId == null) return;

        Map<String, Session> room = chatRooms.get(gameId);
        if (room == null) return;

        String userId = String.valueOf(session.getUserProperties().getOrDefault("userId", session.getId()));
        Session cur = room.get(userId);
        if (cur == session) {
            room.remove(userId);
        }

        if (room.isEmpty()) {
            chatRooms.remove(gameId);
        }

        System.out.println("chat remove(" + reason + "): game=" + gameId + ", user=" + userId + ", sid=" + session.getId());
    }

    private static void safeClose(Session s, String reason) {
        if (s == null) return;
        try {
            s.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, reason));
        } catch (Exception ignore) {}
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
