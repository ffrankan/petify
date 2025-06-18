# Spring Boot 配置管理最佳实践

## 概述

本文档定义了Petify项目中Spring Boot配置管理的最佳实践，基于Spring Boot 2.4+和Spring Cloud Alibaba的官方推荐，实现配置的集中化管理和优雅降级。

## 配置架构设计

### 设计原则

1. **配置外部化** - 敏感配置和环境相关配置统一管理
2. **优雅降级** - 外部配置不可用时本地配置作为fallback
3. **环境隔离** - 不同环境配置完全隔离，避免相互影响
4. **动态刷新** - 支持配置热更新，无需重启服务

### 配置分层结构

#### 1. application.yml (主配置文件)
- **目的**: 包含通用配置和环境无关配置
- **内容**:
  - 应用基础信息（名称、端口）
  - Docker Compose配置
  - Nacos连接配置
  - 配置导入声明

```yaml
server:
  port: 8080

spring:
  application:
    name: petify-gateway
  profiles:
    active: dev
  docker:
    compose:
      enabled: true
      file: compose.yaml
      lifecycle-management: start-only
  cloud:
    nacos:
      server-addr: localhost:8848
      namespace: petify
      username: nacos
      password: nacos
      discovery:
        enabled: true
      config:
        server-addr: localhost:8848
        namespace: petify
        group: DEFAULT_GROUP
        file-extension: yml
  config:
    import:
      - optional:nacos:petify-gateway-dev.yml?group=DEFAULT_GROUP&refresh=true
      - optional:nacos:common-config.yml?group=DEFAULT_GROUP&refresh=true
```

#### 2. application-{profile}.yml (环境特定配置)
- **目的**: 本地fallback配置，确保服务独立启动
- **内容**:
  - 完整的业务配置
  - 当Nacos不可用时的备选配置
  - 开发环境特定设置

```yaml
# Fallback configuration for local development when Nacos is unavailable
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      # ... 完整的gateway配置
  data:
    redis:
      host: localhost
      port: 6379
      # ... Redis配置

management:
  endpoints:
    web:
      exposure:
        include: "*"
# ... 其他业务配置
```

#### 3. Nacos远程配置文件
- **文件名**: `{service-name}-{profile}.yml`
- **目的**: 集中管理的动态配置
- **内容**: 与application-{profile}.yml内容一致

## 配置加载机制

### 加载优先级

1. **Nacos远程配置** (最高优先级)
2. **本地 application-{profile}.yml**
3. **本地 application.yml** (最低优先级)

### Optional导入机制

使用`optional:`前缀确保配置导入失败时不影响应用启动：

```yaml
spring:
  config:
    import:
      - optional:nacos:petify-gateway-dev.yml?group=DEFAULT_GROUP&refresh=true
      - optional:nacos:common-config.yml?group=DEFAULT_GROUP&refresh=true
```

**优势**:
- Nacos可用时：使用远程配置，支持动态刷新
- Nacos不可用时：自动降级到本地配置，确保服务启动

## 实施规范

### 1. 新服务配置规范

创建新微服务时必须遵循以下配置结构：

```
src/main/resources/
├── application.yml              # 主配置文件
├── application-dev.yml          # 开发环境fallback配置
├── application-test.yml         # 测试环境fallback配置
└── application-prod.yml         # 生产环境fallback配置
```

### 2. Nacos配置规范

在Nacos中创建对应的配置文件：

- **Data ID**: `{service-name}-{profile}.yml`
- **Group**: `DEFAULT_GROUP`
- **Namespace**: `petify`
- **Format**: `YAML`

### 3. 配置同步规范

- 本地fallback配置必须与Nacos配置保持同步
- 配置变更时需同时更新Nacos和本地文件
- 通过版本控制管理本地配置文件

## 配置管理流程

### 开发阶段

1. **本地开发**: 直接修改`application-dev.yml`
2. **配置测试**: 验证配置正确性
3. **同步到Nacos**: 将配置推送到Nacos配置中心

### 部署阶段

1. **环境隔离**: 不同环境使用不同的Nacos namespace
2. **配置验证**: 部署前验证Nacos配置完整性
3. **降级测试**: 测试Nacos不可用时的fallback机制

## 安全考虑

### 敏感信息管理

1. **数据库密码**: 存储在Nacos配置中心
2. **JWT密钥**: 通过环境变量或Nacos管理
3. **第三方API密钥**: 加密存储在Nacos中

### 访问控制

1. **Nacos访问**: 配置用户名密码认证
2. **命名空间隔离**: 不同环境使用独立namespace
3. **配置审计**: 记录配置变更历史

## 监控和运维

### 配置监控

1. **配置加载状态**: 通过Actuator监控配置源
2. **Nacos连接状态**: 监控服务注册和配置拉取状态
3. **配置刷新**: 监控动态配置更新事件

### 故障处理

1. **Nacos不可用**: 自动降级到本地配置
2. **配置格式错误**: 服务启动时进行配置验证
3. **配置冲突**: 明确优先级规则，避免配置冲突

## 迁移指南

### 从Bootstrap配置迁移

1. **移除bootstrap.yml**: 删除所有服务的bootstrap配置文件
2. **配置合并**: 将bootstrap配置合并到application.yml
3. **导入配置**: 使用`spring.config.import`替代bootstrap配置加载
4. **测试验证**: 确保配置加载和服务启动正常

### 验证清单

- [ ] application.yml包含Nacos连接配置
- [ ] 使用optional:前缀导入Nacos配置
- [ ] application-{profile}.yml包含完整fallback配置
- [ ] Nacos中存在对应的配置文件
- [ ] 配置加载优先级正确
- [ ] 动态配置刷新功能正常
- [ ] Nacos不可用时服务能正常启动

## 总结

这套配置管理最佳实践实现了：

1. **高可用性**: 通过fallback机制确保服务独立启动
2. **配置集中化**: 统一管理所有环境配置
3. **动态更新**: 支持配置热更新
4. **环境隔离**: 完全隔离不同环境配置
5. **优雅降级**: Nacos不可用时自动降级

遵循这些最佳实践可以显著提升配置管理的可靠性和可维护性。