package com.spdb.sre.controller;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spdb.sre.model.PowerResponse;
import com.spdb.sre.powershell.IPowershellEventListener;
import com.spdb.sre.powershell.ProcessInvoker;

@Controller
public class DemoController {

    @PostMapping("/api/execute")
    @CrossOrigin
    @ResponseBody
    public String execute(@RequestBody String command) throws IOException, InterruptedException {

        ProcessInvoker processInvoker = new ProcessInvoker();
        StringBuilder result = new StringBuilder();

        processInvoker.addListener(new IPowershellEventListener() {

            @Override
            public void handleOutputLine(String stdout) {
                result.append(stdout + "\r\n");
            }

            @Override
            public void handleReturnData(String data) {
            }
        });

        processInvoker.ExecutePsCommandAsync(command, null, null);

        return result.toString();
    }

    @PostMapping("/api/executeMultiLine")
    @CrossOrigin
    @ResponseBody
    public PowerResponse executeMultiLine(@RequestBody String command, @RequestParam String params)
            throws IOException, InterruptedException, ExecutionException {

        ProcessInvoker processInvoker = new ProcessInvoker();
        StringBuilder result = new StringBuilder();
        StringBuilder returnData = new StringBuilder();

        processInvoker.addListener(new IPowershellEventListener() {

            @Override
            public void handleOutputLine(String stdout) {
                result.append(stdout + "\r\n");
            }

            @Override
            public void handleReturnData(String data) {
                returnData.setLength(0);
                returnData.append(data);
            }
        });

        var task = processInvoker.ExecutePsMultiLineWithAgentModuleAsync(command, null, null);

        return new PowerResponse() {
            {
                stdout = result.toString();
                data = returnData.toString();
                exitCode = task.get();
            }
        };
    }
}
