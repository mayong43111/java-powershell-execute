package com.spdb.sre.handler;

import org.springframework.web.socket.WebSocketSession;

import com.spdb.sre.model.WsRequest;

public interface ICommandHandler {

    void execute(WebSocketSession session, WsRequest request);
}
