package com.spdb.sre.handler;

import java.io.IOException;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.spdb.sre.model.WsRequest;

public class WebsocketHandler extends TextWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        WebNotificationBus.addSession(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Gson gson = new Gson();
        WsRequest request = gson.fromJson(message.getPayload(), WsRequest.class);

        var handler = CommandHandlerFactory.get(request.requestType);

        if (null != handler) {
            handler.execute(session, request);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        WebNotificationBus.removeSession(session);
    }
}
