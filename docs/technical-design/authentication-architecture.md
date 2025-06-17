# Petify 认证架构技术设计

## 概述

本文档描述了Petify平台的认证架构设计思路，包括微服务组件交互、JWT双token策略和用户接口交互流程。

## 整体架构

```
┌─────────────────┐    ┌─────────────────┐
│   Web Client    │    │  Mobile Client  │
└─────────┬───────┘    └─────────┬───────┘
          │                      │
          └──────────┬───────────┘
                     │
        ┌─────────────────┐
        │   API Gateway   │  ← JWT验证 + 路由转发
        └─────────┬───────┘
                  │
    ┌─────────────┼─────────────┐
    │             │             │
┌───▼───┐    ┌────▼────┐    ┌───▼───┐
│ User  │    │   Pet   │    │ Appt  │
│Service│    │ Service │    │Service│
└───┬───┘    └─────────┘    └───────┘
    │
┌───▼───┐    ┌─────────┐
│ Redis │    │PostgreSQL│
│(Token)│    │ (UserDB) │
└───────┘    └─────────┘
```

## 微服务组件职责

### API Gateway
- **JWT验证**: 验证Access Token有效性和签名
- **用户解析**: 从Token中提取用户ID、角色等信息
- **请求转发**: 在请求头中添加用户信息，转发给业务服务
- **路由管理**: 根据路径前缀路由到对应服务

### User Service
- **认证管理**: 用户注册、登录、密码管理
- **Token生成**: 生成Access Token + Refresh Token
- **Token刷新**: 使用Refresh Token获取新的Access Token
- **用户信息**: 用户档案、权限、设备管理

### Business Services (Pet/Appointment)
- **信任Gateway**: 直接使用Gateway传递的用户信息
- **业务逻辑**: 专注于业务功能，无需处理认证
- **权限检查**: 验证用户是否有权限操作特定资源

## JWT双Token策略

### Token类型设计

**Access Token (短期)**
- 生命周期: 75分钟
- 用途: API访问凭证
- 载荷: userId, username, roles, permissions
- 存储: 客户端内存，不持久化

**Refresh Token (长期)**
- 生命周期: 90天
- 用途: 刷新Access Token
- 载荷: userId, deviceId, IP地址
- 存储: HttpOnly Cookie + Redis

### Token安全机制

**签名算法**: RS256非对称加密
**Token撤销**: Redis黑名单机制
**设备绑定**: 限制同一账号最多5个设备
**自动刷新**: Access Token过期前自动续期

## 组件交互流程

### 1. 用户登录流程
```
客户端 → Gateway → User Service
  │         │          │
  │    路由转发     验证凭据
  │         │      生成Token
  │         │          │
  │    ←────────────Token返回
  │
存储Access Token(内存)
Refresh Token(Cookie)
```

### 2. API调用流程
```
客户端 → Gateway → Business Service
  │         │            │
JWT请求   验证Token    获取用户信息
  │      解析用户     (从请求头)
  │      添加头部         │
  │         │        执行业务逻辑
  │         │            │
  │    ←─────────────业务结果
```

### 3. Token刷新流程
```
客户端 → Gateway → User Service
  │         │          │
Refresh    路由转发   验证Refresh Token
Token       │      生成新Access Token
  │         │          │
  │    ←────────────新Token返回
```

## 用户接口交互设计

### 认证接口

**用户注册**
```
POST /api/user/auth/register
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123"
}
→ 返回用户基本信息
```

**用户登录**
```
POST /api/user/auth/login
{
  "identifier": "john@example.com", // 邮箱或用户名
  "password": "password123"
}
→ 返回 {
  "accessToken": "jwt-token",
  "refreshToken": "refresh-token",
  "user": {...}
}
```

**Token刷新**
```
POST /api/user/auth/refresh
{
  "refreshToken": "refresh-token"
}
→ 返回新的accessToken
```

### 业务接口调用

**宠物服务调用示例**
```
GET /api/pet/pets
Headers:
  Authorization: Bearer <access-token>
  
→ Gateway验证token
→ 添加请求头: X-User-Id: 12345
→ 转发到Pet Service
→ Pet Service根据X-User-Id返回该用户的宠物列表
```

### 安全特性

- **登录失败锁定**: 5次失败锁定30分钟
- **设备限制**: 同一账号最多5个设备同时在线
- **Token黑名单**: 密码修改、退出登录时撤销token
- **异地登录检测**: IP变化时发送提醒

## 数据存储策略

### Redis缓存用途
- **Token黑名单**: `blacklist:{jti}` (TTL: token过期时间)
- **登录安全**: `login_attempts:{identifier}` (TTL: 30分钟)
- **用户会话**: `user_session:{userId}` (TTL: 2小时)
- **验证码**: `email_code:{email}` (TTL: 10分钟)

### 数据库设计要点
- **用户表**: 支持多种登录方式(邮箱/手机/用户名)
- **Token表**: 存储Refresh Token，支持设备管理
- **权限表**: RBAC角色权限设计
- **日志表**: 登录历史、操作审计

## 关键配置参数

### JWT配置
- **Access Token**: 75分钟过期
- **Refresh Token**: 90天过期
- **签名算法**: RS256非对称加密
- **发行者**: petify-platform

### 安全配置
- **密码策略**: 8-20位，必须包含数字和字母
- **登录限制**: 5次失败锁定30分钟
- **设备限制**: 每用户最多5个并发设备
- **速率限制**: 登录10次/分钟，注册5次/分钟

## 监控与运维

### 关键监控指标
- **认证成功率**: 登录成功/总登录次数
- **Token验证延迟**: Gateway JWT验证响应时间
- **安全事件**: 登录失败、异地登录、Token撤销
- **系统健康**: Redis连接、数据库连接、服务注册状态

### 日志记录
- **用户行为**: 登录、登出、密码修改
- **安全事件**: 登录失败、账号锁定、Token异常
- **系统事件**: 服务启动、配置变更、错误异常

## 实现优先级

### Phase 1: 基础认证
1. 用户注册/登录基础功能
2. JWT双Token生成和验证
3. Gateway基础认证过滤器
4. 简单的权限控制

### Phase 2: 安全增强
1. 登录失败锁定机制
2. Token黑名单和撤销
3. 设备管理和限制
4. 密码安全策略

### Phase 3: 完善功能
1. 多因子认证(MFA)
2. 第三方登录集成
3. 详细的审计日志
4. 高级安全监控

---

这个架构设计提供了清晰的组件职责分工、安全的认证流程和良好的扩展性，符合微服务最佳实践。