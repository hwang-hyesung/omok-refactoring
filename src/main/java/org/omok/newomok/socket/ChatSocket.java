package org.omok.newomok.socket;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/chat/{gameId}")
public class ChatSocket {

    // 게임 ID 별로 세션을 관리하
    private static Map<String, Set<Session>> chatRooms = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("gameId") String gameId) throws IOException {
        // gameId 방이 없으면 새로 만들고, 세션 추가
        chatRooms.putIfAbsent(gameId, Collections.synchronizedSet(new HashSet<>()));
        chatRooms.get(gameId).add(session);

        // 세션에 gameId 저장 (나중에 메시지 처리 시 활용)
        session.getUserProperties().put("gameId", gameId);

        // 접속자에게 초기 메시지 전달
        String init = String.format("{\"senderId\":\"%s\",\"text\":\"__INIT__\"}", session.getId());
        session.getBasicRemote().sendText(init);

        System.out.println("새 연결: 세션ID=" + session.getId() + ", 게임방=" + gameId);
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        String senderId = session.getId();
        String gameId = (String) session.getUserProperties().get("gameId");
        if (gameId == null) {
            // 게임방 정보가 없으면 무시
            return;
        }

        Set<Session> room = chatRooms.get(gameId);
        if (room == null) {
            // 방이 없으면 무시
            return;
        }

        // 메시지 내 특수문자 이스케이프 처리
        String escaped = message
                .replaceAll("\\\\", "\\\\\\\\")
                .replaceAll("\"", "\\\\\"")
                .replaceAll("\n", "\\\\n");

        // JSON 형식 메시지 생성
        String payload = String.format(
                "{\"senderId\":\"%s\",\"text\":\"%s\"}", senderId, escaped
        );

        // 동기화하여 해당 방의 모든 세션에 메시지 전송
        synchronized (room) {
            for (Session s : room) {
                if (s.isOpen()) {
                    s.getBasicRemote().sendText(payload);
                }
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        String gameId = (String) session.getUserProperties().get("gameId");
        if (gameId != null) {
            Set<Session> room = chatRooms.get(gameId);
            if (room != null) {
                room.remove(session);
                if (room.isEmpty()) {
                    // 방이 비면 맵에서 제거 (메모리 누수 방지)
                    chatRooms.remove(gameId);
                    System.out.println("게임방 " + gameId + "이 비어서 제거됨");
                }
            }
        }

        System.out.println("연결 종료: 세션ID=" + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        System.err.println("에러 발생: 세션ID=" + (session != null ? session.getId() : "null"));
        thr.printStackTrace();
    }
}
