# Petify Gateway Service

## 概述

Petify Gateway 是基于 Spring Cloud Gateway 构建的响应式 API 网关服务，为 Petify 宠物管理平台提供统一的入口点和流量管理。

## 架构特性

### 响应式架构
- **非阻塞 I/O**: 基于 Spring WebFlux 和 Netty 的响应式编程模型
- **高并发处理**: 异步非阻塞处理，支持大量并发请求
- **弹性连接池**: 自适应连接池管理，优化资源利用

### 核心功能
- **服务路由**: 智能路由到后端微服务
- **负载均衡**: 基于 Spring Cloud LoadBalancer 的负载均衡
- **熔断保护**: Resilience4j 熔断器防止级联故障
- **限流控制**: Redis 分布式限流
- **监控日志**: 全链路请求追踪和监控

## 服务配置

### 端口配置
- **网关端口**: 8080
- **管理端口**: 8080/actuator/*

### 依赖服务
- **Nacos**: 服务注册发现 (localhost:8848)
- **Redis**: 限流存储 (localhost:6379)
- **后端服务**: User、Pet、Appointment 服务

## 路由配置

### 服务路由映射

| 路径前缀 | 目标服务 | 描述 |
|---------|---------|------|
| `/api/user/**` | petify-user-service | 用户认证和管理 |
| `/api/pet/**` | petify-pet-service | 宠物信息管理 |
| `/api/appointment/**` | petify-appointment-service | 预约服务管理 |
| `/health` | 内置健康检查 | 网关健康状态 |

### 路由特性
- **路径重写**: 自动移除前两级路径前缀
- **服务发现**: 基于 Nacos 的动态服务发现
- **CORS 支持**: 全局跨域资源共享配置

## 响应式特性

### 熔断器配置
```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        slidingWindowSize: 10          # 滑动窗口大小
        minimumNumberOfCalls: 5        # 最小调用次数
        failureRateThreshold: 50       # 失败率阈值 (50%)
        waitDurationInOpenState: 30s   # 熔断器打开状态等待时间
```

### 限流配置
- **限流策略**: 每用户每秒 10 请求，突发 20 请求
- **存储方式**: Redis 分布式存储
- **识别方式**: 基于 X-User-ID 头或客户端 IP

### 重试机制
- **重试次数**: 最多 3 次
- **重试间隔**: 1 秒
- **重试条件**: 连接异常、超时异常

## 监控和健康检查

### Actuator 端点
- `/actuator/health` - 服务健康状态
- `/actuator/gateway/routes` - 路由信息
- `/actuator/metrics` - 性能指标
- `/actuator/circuitbreakers` - 熔断器状态

### 日志配置
```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    reactor.netty: INFO
    org.springframework.web.reactive: DEBUG
```

## 部署指南

### 本地开发启动

#### 方式一: Maven 启动 (推荐)
```bash
# 自动启动依赖的 Docker 服务
mvn spring-boot:run -pl petify-gateway
```

#### 方式二: Docker Compose
```bash
# 先启动基础设施服务
docker-compose up -d nacos nacos-mysql postgres redis

# 启动网关服务
docker-compose up -d gateway
```

### 构建和打包
```bash
# 构建 JAR 包
mvn clean package -DskipTests -pl petify-gateway

# 构建 Docker 镜像
mvn clean package docker:build -DskipTests -pl petify-gateway
```

## 配置文件说明

### bootstrap.yml
- **服务注册**: Nacos 服务发现配置
- **网关路由**: 基础路由和过滤器配置
- **HTTP 客户端**: 响应式 HTTP 客户端配置

### application.yml
- **熔断器**: Resilience4j 熔断器详细配置
- **重试策略**: 服务调用重试配置
- **日志级别**: 各组件日志级别设置

## 故障处理

### 熔断降级
当后端服务不可用时，网关自动触发熔断机制：

| 服务 | 降级端点 | 响应 |
|-----|---------|------|
| 用户服务 | `/fallback/user` | 用户服务暂时不可用 |
| 宠物服务 | `/fallback/pet` | 宠物服务暂时不可用 |
| 预约服务 | `/fallback/appointment` | 预约服务暂时不可用 |

### 限流处理
当请求超过限流阈值时：
- **HTTP 状态码**: 429 Too Many Requests
- **响应头**: X-RateLimit-Remaining
- **重试建议**: Retry-After 头

## 性能优化

### 连接池配置
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          type: elastic              # 弹性连接池
          max-idle-time: 15s        # 最大空闲时间
          max-life-time: 60s        # 连接最大存活时间
```

### 缓存策略
- **路由缓存**: 动态路由信息缓存
- **服务发现缓存**: Nacos 服务列表缓存
- **限流计数器**: Redis 分布式计数器

## 安全配置

### CORS 配置
```yaml
globalcors:
  corsConfigurations:
    '[/**]':
      allowedOrigins: "*"
      allowedMethods: "*"
      allowedHeaders: "*"
```

### 请求头增强
- `X-Gateway-Type: Reactive` - 标识响应式网关
- `X-Gateway-Timestamp` - 请求时间戳
- `X-User-ID` - 用户标识 (用于限流)

## 故障排查

### 常见问题

1. **服务发现失败**
   - 检查 Nacos 连接: `curl http://localhost:8848/nacos`
   - 验证服务注册: 查看 Nacos 控制台

2. **限流异常**
   - 检查 Redis 连接: `redis-cli ping`
   - 查看限流日志: `/actuator/loggers`

3. **熔断器激活**
   - 查看熔断状态: `/actuator/circuitbreakers`
   - 检查后端服务健康: `/actuator/health`

### 日志分析
```bash
# 查看网关日志
docker-compose logs -f gateway

# 查看特定时间段日志
docker-compose logs --since="2024-01-01T00:00:00" gateway
```

## 扩展开发

### 自定义过滤器
参考 `RequestLoggingGatewayFilterFactory` 实现自定义过滤器：

```java
@Component
public class CustomGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {
    // 自定义过滤器实现
}
```

### 动态路由
通过 Nacos 配置中心实现动态路由配置：

```yaml
# 在 Nacos 中配置 gateway-routes.yml
spring:
  cloud:
    gateway:
      routes:
        - id: dynamic-route
          uri: lb://new-service
          predicates:
            - Path=/api/new/**
```

## 版本信息
- **Spring Boot**: 3.2.5
- **Spring Cloud**: 2022.0.0.0
- **Spring Cloud Gateway**: 响应式版本
- **Resilience4j**: 熔断器和重试
- **Java**: 17+