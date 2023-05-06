package com.spdb.sre.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spdb.sre.model.PowerStdoutResponse;
import com.spdb.sre.powershell.PowershellExecutor;

@Controller
public class DemoController {

    @PostMapping("/api/execute")
    @CrossOrigin
    @ResponseBody
    public String execute(@RequestBody String command) throws IOException, InterruptedException {

        return PowershellExecutor.execute(command, false);
    }

    @PostMapping("/api/executeMultiLine")
    @CrossOrigin
    @ResponseBody
    public String executeMultiLine(@RequestBody String command, @RequestParam String params)
            throws IOException, InterruptedException, ExecutionException {

        return PowershellExecutor.execute(command, true);
    }

    @GetMapping("api/getStdout")
    @CrossOrigin
    @ResponseBody
    public PowerStdoutResponse getStdout(@RequestParam String requestId, @RequestParam int currentLine) {

        String currentDirectory = System.getProperty("user.dir");

        String fileName = Paths.get(currentDirectory)
                .resolve(Paths.get(".\\logs\\" + requestId + "\\stdout.txt")).normalize().toString();

        File file = new File(fileName);
        if (!file.exists()) {
            var res = new PowerStdoutResponse();

            res.completed = true;
            res.line = "requestId not found\r\n";

            return res;
        }

        StringBuilder lines = new StringBuilder();
        boolean completed = false; // DEMO 从文件中获取

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int lineNumber = 0;

            // 读取指定开始行及其之后的所有行
            while ((line = br.readLine()) != null) {

                if (line.startsWith(">>>###end###<<<")) {
                    completed = true;
                    break;
                }

                if (lineNumber < currentLine) {
                    lineNumber++;
                    continue;
                }

                lineNumber++;
                lines.append(line + "\r\n");
            }

            var res = new PowerStdoutResponse();
            res.completed = completed;
            res.line = lines.toString();
            res.currentLine = lineNumber;

            return res;

        } catch (Exception e) {
            e.printStackTrace();
        }

        var res = new PowerStdoutResponse();

        res.completed = true;
        res.line = "error\r\n";

        return res;
    }
}
