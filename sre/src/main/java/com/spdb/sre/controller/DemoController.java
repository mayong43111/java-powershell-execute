package com.spdb.sre.controller;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
}
