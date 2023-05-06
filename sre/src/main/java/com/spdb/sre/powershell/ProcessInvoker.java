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
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

@Service
public class ProcessInvoker {

    private List<IPowershellEventListener> _listeners = new ArrayList<>();
    private Process _proc;

    @Async
    public CompletableFuture<Integer> ExecutePsMultiLineWithAgentModuleAsync(
            String scripString,
            String workingDirectory,
            Map<String, String> environment) throws IOException, InterruptedException {

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

    @Async
    public CompletableFuture<Integer> ExecutePsWithAgentModuleAsync(
            String filePath,
            String workingDirectory,
            Map<String, String> environment) throws IOException, InterruptedException {

        List<String> command = new ArrayList<>();
        command.add("Import-Module " + getStaticFilePath("\\powershell\\SreCommon.psm1"));
        command.add("&\"" + filePath + "\"");

        return ExecuteAsync(
                List.of("powershell.exe", "-ExecutionPolicy", "Bypass", "-Command", String.join(";", command)),
                workingDirectory,
                environment);
    }

    @Async
    public CompletableFuture<Integer> ExecutePsFileAsync(
            String filePath,
            String workingDirectory,
            Map<String, String> environment) throws IOException, InterruptedException {

        return ExecuteAsync(
                List.of("powershell.exe", "-ExecutionPolicy", "Bypass", "-File", filePath),
                workingDirectory,
                environment);
    }

    @Async
    public CompletableFuture<Integer> ExecutePsCommandAsync(
            String command,
            String workingDirectory,
            Map<String, String> environment) throws IOException, InterruptedException {

        return ExecuteAsync(
                List.of("powershell.exe", "-Command", command),
                workingDirectory,
                environment);
    }

    @Async
    public CompletableFuture<Integer> ExecuteAsync(
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

        var result = _proc.waitFor();

        return CompletableFuture.completedFuture(result);
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

    private static String getStaticFilePath(String fileName) throws IOException {
        File resource = ResourceUtils.getFile("classpath:static/" + fileName);
        return resource.getAbsolutePath();
    }
}
