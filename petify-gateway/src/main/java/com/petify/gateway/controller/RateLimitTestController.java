package com.petify.gateway.controller;

import com.petify.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 限流测试控制器
 * 用于测试和验证Gateway的限流功能和429响应格式
 * 
 * 注意：此控制器仅用于开发测试，生产环境应当移除
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class RateLimitTestController {

    /**
     * 简单的测试端点
     * 用于验证限流是否正常工作
     */
    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        log.info("Rate limit test endpoint accessed at: {}", LocalDateTime.now());
        
        return Result.success(Map.of(
            "message", "Ping successful",
            "timestamp", LocalDateTime.now(),
            "service", "gateway",
            "info", "This endpoint is rate limited. Exceed the limit to see 429 response."
        ));
    }

    /**
     * 快速连续调用测试端点
     * 返回当前时间戳，便于测试客户端识别不同的响应
     */
    @GetMapping("/rapid")
    public Result<Map<String, Object>> rapidTest() {
        long timestamp = System.currentTimeMillis();
        log.info("Rapid test endpoint accessed at timestamp: {}", timestamp);
        
        return Result.success(Map.of(
            "timestamp", timestamp,
            "message", "Rapid test response",
            "note", "Call this endpoint rapidly to trigger rate limiting"
        ));
    }

    /**
     * 返回限流配置信息
     * 帮助测试人员了解当前的限流设置
     */
    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        return Result.success(Map.of(
            "rateLimiting", Map.of(
                "global", Map.of(
                    "replenishRate", 10,
                    "burstCapacity", 20,
                    "requestedTokens", 1,
                    "retryAfterSeconds", 60,
                    "enabled", true
                )
            ),
            "testInstructions", Map.of(
                "step1", "Call /test/rapid multiple times quickly to trigger rate limiting",
                "step2", "Observe the 429 response with proper headers",
                "step3", "Wait for the retry-after period and try again",
                "note", "Rate limiting is applied per IP address or User ID"
            )
        ));
    }
}