package com.petify.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petify.common.result.Result;
import com.petify.gateway.config.RateLimitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * 全局异常处理器，符合RFC 6585标准的429响应处理
 * 处理Spring Cloud Gateway的限流和其他异常
 */
@Slf4j
@Order(-1)
@Component
@RequiredArgsConstructor
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;
    private final RateLimitProperties rateLimitProperties;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // Handle different types of exceptions
        if (isRateLimitException(ex)) {
            return handleRateLimitException(exchange, ex);
        } else if (ex instanceof NotFoundException) {
            return handleNotFoundException(exchange, ex);
        } else if (ex instanceof ResponseStatusException statusEx) {
            return handleResponseStatusException(exchange, statusEx);
        } else {
            return handleGenericException(exchange, ex);
        }
    }

    /**
     * 判断是否为限流异常
     */
    private boolean isRateLimitException(Throwable ex) {
        if (ex instanceof ResponseStatusException statusEx) {
            return statusEx.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
        }
        
        // Spring Cloud Gateway rate limiter抛出的异常通常包含特定消息
        String message = ex.getMessage();
        return message != null && (
            message.contains("Request rate limit exceeded") ||
            message.contains("rate limit") ||
            message.contains("429") ||
            ex.getClass().getSimpleName().contains("RateLimit")
        );
    }

    /**
     * 处理限流异常 - 符合RFC 6585标准
     */
    private Mono<Void> handleRateLimitException(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        // 设置HTTP状态码
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        
        // 设置标准HTTP头
        response.getHeaders().set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        response.getHeaders().set("Cache-Control", "no-store"); // RFC 6585要求
        
        // 设置标准Rate Limit头 - 符合draft-ietf-httpapi-ratelimit-headers
        int retryAfterSeconds = rateLimitProperties.getGlobal().getRetryAfterSeconds();
        response.getHeaders().set("Retry-After", String.valueOf(retryAfterSeconds));
        response.getHeaders().set("X-RateLimit-Limit", "1");
        response.getHeaders().set("X-RateLimit-Remaining", "0");
        response.getHeaders().set("X-RateLimit-Reset", String.valueOf(Instant.now().getEpochSecond() + retryAfterSeconds));
        
        // 创建符合应用标准的响应体
        RateLimitErrorResponse errorResponse = new RateLimitErrorResponse(
            "RATE_LIMIT_EXCEEDED",
            "Rate limit exceeded. Please try again in " + retryAfterSeconds + " seconds.",
            retryAfterSeconds,
            Instant.now().getEpochSecond() + retryAfterSeconds
        );
        
        Result<Object> result = Result.error(429, errorResponse.message());
        
        try {
            String body = objectMapper.writeValueAsString(result);
            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
            
            log.warn("Rate limit exceeded for request: {} {}", 
                exchange.getRequest().getMethod(), 
                exchange.getRequest().getURI());
            
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error serializing rate limit response", e);
            return response.setComplete();
        }
    }

    /**
     * 处理服务未找到异常
     */
    private Mono<Void> handleNotFoundException(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.NOT_FOUND);
        response.getHeaders().set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        
        Result<Object> result = Result.error(404, "Service not found");
        return writeResponse(response, result, ex);
    }

    /**
     * 处理ResponseStatusException
     */
    private Mono<Void> handleResponseStatusException(ServerWebExchange exchange, ResponseStatusException ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(ex.getStatusCode());
        response.getHeaders().set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        
        String message = ex.getReason() != null ? ex.getReason() : "Request failed";
        Result<Object> result = Result.error(ex.getStatusCode().value(), message);
        return writeResponse(response, result, ex);
    }

    /**
     * 处理通用异常
     */
    private Mono<Void> handleGenericException(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        response.getHeaders().set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        
        Result<Object> result = Result.error(500, "Internal server error");
        return writeResponse(response, result, ex);
    }

    /**
     * 写入响应
     */
    private Mono<Void> writeResponse(ServerHttpResponse response, Result<Object> result, Throwable ex) {
        try {
            String body = objectMapper.writeValueAsString(result);
            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
            
            log.error("Gateway error: {}", ex.getMessage(), ex);
            
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response", e);
            return response.setComplete();
        }
    }

    /**
     * 限流错误响应详情
     */
    public record RateLimitErrorResponse(
        String errorCode,
        String message,
        int retryAfterSeconds,
        long resetTimestamp
    ) {}
}