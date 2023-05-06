package com.spdb.sre.handler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.Gson;
import com.spdb.sre.model.WsResponse;
import com.spdb.sre.model.WsResponseType;

public class WebNotificationBus {

    private static List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public static void addSession(WebSocketSession session) {
        sessions.add(session);
    }

    public static void removeSession(WebSocketSession session) {
        sessions.remove(session);
    }

    public static void sendMessageToAll(String requestId, WsResponseType responseType, Object source)
            throws IOException {

        Gson gson = new Gson();
        String message = gson.toJson(source);

        sendMessageToAll(requestId, responseType, message);
    }

    public static void sendMessageToAll(String requestId, WsResponseType responseType, String message)
            throws IOException {

        var response = new WsResponse();
        response.requestId = requestId;
        response.responseType = responseType;
        response.data = message;

        Gson gson = new Gson();
        message = gson.toJson(response);

        sendResponseToAll(message);
    }

    private static void sendResponseToAll(String message) throws IOException {
        for (WebSocketSession session : sessions) {
            session.sendMessage(new TextMessage(message));
        }
    }
}
