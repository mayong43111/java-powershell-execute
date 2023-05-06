package com.spdb.sre.powershell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.util.ResourceUtils;

public class ProcessInvoker {

    private ExecutorService _executorService = Executors.newFixedThreadPool(10);
    private List<IPowershellEventListener> _listeners = new ArrayList<>();
    private Process _proc;

    public Runnable ExecutePsMultiLineWithAgentModuleAsync(
            String scripString,
            String workingDirectory,
            Map<String, String> environment) throws IOException {

        if (workingDirectory == null || workingDirectory.isEmpty()) {
            workingDirectory = System.getProperty("user.dir");
        }

        Path resolvedPath = Paths.get(workingDirectory)
                .resolve(Paths.get(".\\" + UUID.randomUUID().toString() + ".ps1")).normalize();
        String filePath = resolvedPath.toString();

        File file = new File(filePath);
        FileWriter writer = new FileWriter(file);
        writer.append(scripString);
        writer.close();

        var result = ExecutePsWithAgentModuleAsync(
                filePath,
                workingDirectory,
                environment);

        file.delete();

        return result;
    }

    public Runnable ExecutePsWithAgentModuleAsync(
            String filePath,
            String workingDirectory,
            Map<String, String> environment) throws IOException {

        List<String> command = new ArrayList<>();
        command.add("Import-Module " + getStaticFilePath("\\powershell\\SreCommon.psm1"));
        command.add("&\"" + filePath + "\"");

        return ExecuteAsync(
                List.of("powershell.exe", "-ExecutionPolicy", "Bypass", "-Command", String.join(";", command)),
                workingDirectory,
                environment);
    }

    public Runnable ExecutePsFileAsync(
            String filePath,
            String workingDirectory,
            Map<String, String> environment) {

        return ExecuteAsync(
                List.of("powershell.exe", "-ExecutionPolicy", "Bypass", "-File", filePath),
                workingDirectory,
                environment);
    }

    public Runnable ExecutePsCommandAsync(
            String command,
            String workingDirectory,
            Map<String, String> environment) {

        return ExecuteAsync(
                List.of("powershell.exe", "-Command", command),
                workingDirectory,
                environment);
    }

    public Runnable ExecuteAsync(
            List<String> command,
            String workingDirectory,
            Map<String, String> environment) {

        var task = new Runnable() {
            @Override
            public void run() {
                try {
                    Execute(command, workingDirectory, environment);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // 在线程池中启动一个新线程
        _executorService.execute(task);
        return task;
    }

    private void Execute(
            List<String> command,
            String workingDirectory,
            Map<String, String> environment) throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(command);

        pb.redirectErrorStream(true);

        if (workingDirectory != null && !workingDirectory.isEmpty()) {
            pb.directory(new File(workingDirectory));
        }

        if (environment != null && !environment.isEmpty()) {
            Map<String, String> env = pb.environment();
            env.putAll(environment);
        }

        _proc = pb.start();

        StringBuilder data = new StringBuilder();
        boolean isDataStream = false;

        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(_proc.getInputStream(), StandardCharsets.UTF_8))) {
            // 循环等待进程输出，判断进程存活则循环获取输出流数据
            while (_proc.isAlive()) {
                while (bufferedReader.ready()) {
                    String line = bufferedReader.readLine();

                    if (line.startsWith(">>>>begin data>>>>")) {
                        data.setLength(0);
                        isDataStream = true;
                        continue;
                    } else if (line.startsWith("<<<<<end data<<<<<<")) {
                        isDataStream = false;
                        if (data.length() > 0) {
                            publishReturnDataEvent(data.toString());
                        }
                        continue;
                    }

                    if (isDataStream) {
                        data.append(line + "\r\n");
                        continue;
                    } else {
                        publishOutputLineEvent(line);
                    }

                }
            }
        }

        var exitCode = _proc.waitFor();
        publishCompletedEvent(exitCode);
    }

    public boolean Cancellation() {

        if (_proc != null) {
            _proc.destroyForcibly();
        }

        return true;
    }

    public void addListener(IPowershellEventListener listener) {
        _listeners.add(listener);
    }

    public void removeListener(IPowershellEventListener listener) {
        _listeners.remove(listener);
    }

    private void publishOutputLineEvent(String output) {
        for (IPowershellEventListener listener : _listeners) {
            listener.handleOutputLine(output);
        }
    }

    private void publishReturnDataEvent(String data) {
        for (IPowershellEventListener listener : _listeners) {
            listener.handleReturnData(data);
        }
    }

    private void publishCompletedEvent(int exitCode) {
        for (IPowershellEventListener listener : _listeners) {
            listener.handleCompleted(exitCode);
        }
    }

    private static String getStaticFilePath(String fileName) throws IOException {
        File resource = ResourceUtils.getFile("classpath:static/" + fileName);
        return resource.getAbsolutePath();
    }
}
