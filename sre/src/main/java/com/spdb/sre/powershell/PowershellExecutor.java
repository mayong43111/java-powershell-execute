package com.spdb.sre.powershell;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

import com.spdb.sre.handler.WebNotificationBus;
import com.spdb.sre.model.PowerResponse;
import com.spdb.sre.model.WsResponseType;

public class PowershellExecutor {

    public static String execute(String command, boolean multiLine) throws IOException {

        String requestId = UUID.randomUUID().toString();

        // begin 因为DEMO没有数据库，所以用文件来保存执行的 stdout
        String currentDirectory = System.getProperty("user.dir");

        String workingDirectoryPath = Paths.get(currentDirectory)
                .resolve(Paths.get(".\\logs\\" + requestId)).normalize().toString();

        File workingDirectory = new File(workingDirectoryPath);
        if (!workingDirectory.exists()) {
            workingDirectory.mkdirs();
        }

        File stdoutFile = new File(workingDirectory, "stdout.txt");
        FileWriter writer = new FileWriter(stdoutFile);
        // end

        ProcessInvoker processInvoker = new ProcessInvoker();

        processInvoker.addListener(new AbstractPowershellEventListener() {

            StringBuilder returnData = new StringBuilder();

            @Override
            public void handleOutputLine(String stdout) {
                try {
                    writer.append(stdout + "\r\n");
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void handleReturnData(String data) {
                returnData.setLength(0);
                returnData.append(data);
            }

            @Override
            public void handleCompleted(int code) {
                try {
                    // 因为没有数据库，所以这里往文件里写入一个结束标识位代替数据库的状态位
                    writer.append(">>>###end###<<<");
                    writer.close();

                    // TODO: 通知完成，这里通应该做过滤，通知哪些人
                    WebNotificationBus.sendMessageToAll(
                            requestId,
                            WsResponseType.ExecutePowershellCompleted,
                            new PowerResponse() {
                                {
                                    data = returnData.toString();
                                    exitCode = code;
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        if (multiLine) {

            processInvoker
                    .ExecutePsMultiLineWithAgentModuleAsync(command, workingDirectoryPath, null);
        } else {

            processInvoker.ExecutePsCommandAsync(command, workingDirectoryPath, null);
        }

        return requestId;
    }
}
