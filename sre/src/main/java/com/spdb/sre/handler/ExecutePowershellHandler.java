package com.spdb.sre.handler;

import java.io.IOException;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.spdb.sre.model.WsRequest;
import com.spdb.sre.powershell.IPowershellEventListener;
import com.spdb.sre.powershell.ProcessInvoker;

public class ExecutePowershellHandler implements ICommandHandler {

    @Override
    public void execute(WebSocketSession session, WsRequest request) {

        try {
            ProcessInvoker processInvoker = new ProcessInvoker();

            processInvoker.addListener(new IPowershellEventListener() {

                @Override
                public void handleOutputLine(String stdout) {
                    try {
                        session.sendMessage(new TextMessage(stdout + "\r\n"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void handleReturnData(String data) {
                }
            });

            processInvoker.ExecutePsMultiLineWithAgentModuleAsync(request.data, null, null);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
