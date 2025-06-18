package com.petify.gateway.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Spring Cloud Gateway反应式网关配置
 * 配置微服务路由规则、限流、熔断器、重试等中间件功能
 */
@Configuration
@RequiredArgsConstructor
public class ReactiveGatewayConfig {

    private final RateLimitProperties rateLimitProperties;

    /**
     * 网关超时时间配置，默认30秒
     */
    @Value("${gateway.timeout:30s}")
    private Duration timeout;

    /**
     * 自定义路由定位器配置
     * 定义所有微服务的路由规则和过滤器链
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 用户服务路由配置 - 处理用户认证、用户管理等功能
                .route("user-service", r -> r.path("/api/user/**")
                        .filters(f -> f
                                .stripPrefix(2)  // 移除路径前缀 /api/user -> /
                                .retry(3)        // 失败重试3次
                                .circuitBreaker(config -> config
                                        .setName("user-service-cb")
                                        .setFallbackUri("forward:/fallback/user"))  // 熔断器配置
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())         // 统一限流器
                                        .setKeyResolver(userKeyResolver()))         // 限流Key解析器
                                .filter(requestLoggingFilter().apply(new RequestLoggingGatewayFilterFactory.Config())))  // 请求日志过滤器
                        .uri("lb://petify-user-service"))  // 负载均衡到用户服务

                // 宠物服务路由配置 - 处理宠物信息、品种、医疗记录等功能
                .route("pet-service", r -> r.path("/api/pet/**")
                        .filters(f -> f
                                .stripPrefix(2)  // 移除路径前缀 /api/pet -> /
                                .retry(3)        // 失败重试3次
                                .circuitBreaker(config -> config
                                        .setName("pet-service-cb")
                                        .setFallbackUri("forward:/fallback/pet"))   // 熔断器配置
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())         // 统一限流器
                                        .setKeyResolver(userKeyResolver()))         // 限流Key解析器
                                .filter(requestLoggingFilter().apply(new RequestLoggingGatewayFilterFactory.Config())))  // 请求日志过滤器
                        .uri("lb://petify-pet-service"))   // 负载均衡到宠物服务

                // 预约服务路由配置 - 处理预约管理、服务提供商、评价等功能
                .route("appointment-service", r -> r.path("/api/appointment/**")
                        .filters(f -> f
                                .stripPrefix(2)  // 移除路径前缀 /api/appointment -> /
                                .retry(3)        // 失败重试3次
                                .circuitBreaker(config -> config
                                        .setName("appointment-service-cb")
                                        .setFallbackUri("forward:/fallback/appointment"))  // 熔断器配置
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())         // 统一限流器
                                        .setKeyResolver(userKeyResolver()))         // 限流Key解析器
                                .filter(requestLoggingFilter().apply(new RequestLoggingGatewayFilterFactory.Config())))  // 请求日志过滤器
                        .uri("lb://petify-appointment-service"))  // 负载均衡到预约服务

                // 健康检查路由 - 网关自身健康状态检查
                .route("health-check", r -> r.path("/health")
                        .filters(f -> f
                                .setResponseHeader("X-Gateway-Status", "UP")  // 设置响应头
                                .filter((exchange, chain) -> {
                                    exchange.getResponse().setStatusCode(HttpStatus.OK);
                                    return exchange.getResponse().setComplete();  // 直接返回200状态
                                }))
                        .uri("no://op"))  // 无实际后端服务
                .build();
    }

    /**
     * Redis限流器配置 - 统一限流策略
     * 基于配置文件的动态参数设置
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        var config = rateLimitProperties.getGlobal();
        return new RedisRateLimiter(
            config.getReplenishRate(),
            config.getBurstCapacity(),
            config.getRequestedTokens()
        );
    }

    /**
     * 限流Key解析器配置
     * 优先使用请求头中的X-User-ID，如果没有则使用客户端IP地址
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // 优先使用用户ID进行限流 - 使用var简化类型声明
            var userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
            if (userId != null) {
                return Mono.just(userId);
            }
            // 如果没有用户ID，则使用客户端IP地址，增加空值保护
            var remoteAddress = exchange.getRequest().getRemoteAddress();
            var hostAddress = remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
            return Mono.just(hostAddress);
        };
    }

    /**
     * 请求日志过滤器工厂Bean
     * 用于记录每个请求的处理过程
     */
    @Bean
    public RequestLoggingGatewayFilterFactory requestLoggingFilter() {
        return new RequestLoggingGatewayFilterFactory();
    }

    /**
     * 请求日志网关过滤器工厂
     * 负责为每个请求添加时间戳并记录请求处理过程
     */
    public static class RequestLoggingGatewayFilterFactory extends AbstractGatewayFilterFactory<RequestLoggingGatewayFilterFactory.Config> {

        private static final Logger log = LoggerFactory.getLogger(RequestLoggingGatewayFilterFactory.class);

        /**
         * 构造函数，指定配置类
         */
        public RequestLoggingGatewayFilterFactory() {
            super(Config.class);
        }

        /**
         * 应用过滤器逻辑
         * @param config 过滤器配置
         * @return GatewayFilter 网关过滤器
         */
        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                // 修改请求，添加网关时间戳头 - 使用var简化类型声明
                var modifiedExchange = exchange.mutate()
                        .request(originalRequest -> originalRequest
                                .header("X-Gateway-Timestamp", String.valueOf(System.currentTimeMillis())))
                        .build();

                // 继续处理请求并添加响应式钩子函数
                return chain.filter(modifiedExchange)
                        .doOnSubscribe(subscription ->
                                log.info("Reactive Gateway: Processing request to {}", 
                                        exchange.getRequest().getURI()))
                        .doOnSuccess(result ->
                                log.info("Reactive Gateway: Request completed successfully"))
                        .doOnError(error ->
                                log.error("Reactive Gateway: Request failed - {}", error.getMessage()));
            };
        }

        /**
         * 过滤器配置类
         * 可以在此添加配置属性
         */
        public static class Config {
            // Configuration properties can be added here
        }
    }
}