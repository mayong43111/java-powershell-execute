package com.spdb.sre.handler;

import java.io.IOException;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.spdb.sre.model.WsRequest;
import com.spdb.sre.powershell.AbstractPowershellEventListener;
import com.spdb.sre.powershell.ProcessInvoker;

public class ExecutePowershellHandler implements ICommandHandler {

    @Override
    public void execute(WebSocketSession session, WsRequest request) {

        try {
            ProcessInvoker processInvoker = new ProcessInvoker();

            processInvoker.addListener(new AbstractPowershellEventListener() {

                @Override
                public void handleOutputLine(String stdout) {
                    try {
                        session.sendMessage(new TextMessage(stdout + "\r\n"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            processInvoker.ExecutePsMultiLineWithAgentModuleAsync(request.data, null, null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
