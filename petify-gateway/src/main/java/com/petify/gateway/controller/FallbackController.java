package com.petify.gateway.controller;

import com.petify.common.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    /**
     * 统一服务降级处理 - 使用Java 17 Switch Expression
     * 使用@ResponseStatus注解自动设置HTTP状态码
     */
    @GetMapping("/{service}")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)  // 503状态码
    public Mono<Result<Object>> serviceFallback(@PathVariable String service) {
        var message = switch (service) {
            case "user" -> "User Service is temporarily unavailable";
            case "pet" -> "Pet Service is temporarily unavailable";
            case "appointment" -> "Appointment Service is temporarily unavailable";
            default -> "Service is temporarily unavailable";
        };
        return Mono.just(Result.error(503, message));
    }

    /**
     * 用户服务降级 - 503服务不可用
     */
    @GetMapping("/user")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Result<Object>> userServiceFallback() {
        return Mono.just(Result.error(503, "User Service is temporarily unavailable"));
    }

    /**
     * 宠物服务降级 - 503服务不可用
     */
    @GetMapping("/pet")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Result<Object>> petServiceFallback() {
        return Mono.just(Result.error(503, "Pet Service is temporarily unavailable"));
    }

    /**
     * 预约服务降级 - 503服务不可用
     */
    @GetMapping("/appointment")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Result<Object>> appointmentServiceFallback() {
        return Mono.just(Result.error(503, "Appointment Service is temporarily unavailable"));
    }
}