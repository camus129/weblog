package com.fy.weblog.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fy.weblog.model.dto.Result;

@RestController
public class HealthController {

    @GetMapping("/")
    public Result<String> health() {
        return Result.ok("Weblog API 服务运行正常");
    }

    @GetMapping("/health")
    public Result<String> healthCheck() {
        return Result.ok("OK");
    }
}
