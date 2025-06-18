package com.petify.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 限流配置属性类
 * 统一的Gateway限流参数配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway.rate-limit")
public class RateLimitProperties {

    /**
     * 全局限流配置
     */
    private GlobalRateLimit global = new GlobalRateLimit();

    /**
     * 全局限流配置
     */
    @Data
    public static class GlobalRateLimit {
        /**
         * 令牌桶每秒补充速率
         */
        private int replenishRate = 10;

        /**
         * 令牌桶容量（突发容量）
         */
        private int burstCapacity = 20;

        /**
         * 每次请求消耗的令牌数
         */
        private int requestedTokens = 1;

        /**
         * 重试延迟时间（秒）
         */
        private int retryAfterSeconds = 60;

        /**
         * 是否启用限流功能
         */
        private boolean enabled = true;

        /**
         * 限流Key的前缀
         */
        private String keyPrefix = "rate_limit";

        /**
         * Redis键过期时间（秒）
         */
        private int keyExpiration = 3600;
    }
}