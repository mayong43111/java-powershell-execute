package com.spdb.sre.handler;

import java.io.IOException;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.spdb.sre.model.WsRequest;
import com.spdb.sre.powershell.PowershellExecutor;

public class ExecutePowershellHandler implements ICommandHandler {

    @Override
    public void execute(WebSocketSession session, WsRequest request) {

        try {
            PowershellExecutor.execute(request.data, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
