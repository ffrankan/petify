# User Service 产品需求文档

## 项目概述

User Service 是 Petify 宠物管理平台的认证与用户管理核心服务，负责用户注册、登录、JWT认证、用户信息管理等功能。作为整个平台的安全基础，为其他业务服务提供统一的身份认证和授权支持。

## 核心功能模块

### 1. 用户认证管理

#### 1.1 用户注册
- **功能描述**: 新用户账号创建，支持多种注册方式
- **注册方式**:
  - 邮箱注册: 邮箱 + 密码 + 验证码
  - 手机注册: 手机号 + 密码 + 短信验证码
  - 第三方注册: 微信、QQ等社交登录(未来功能)
- **核心字段**:
  - 必填: 用户名、密码、邮箱/手机号
  - 可选: 真实姓名、头像、地区
- **业务规则**:
  - 用户名全局唯一，3-20字符
  - 密码强度要求: 8-20位，包含数字+字母
  - 邮箱/手机号验证必须通过
  - 支持邀请码注册(可选功能)

#### 1.2 用户登录
- **功能描述**: 多种方式安全登录，生成JWT token
- **登录方式**:
  - 用户名/邮箱/手机号 + 密码
  - 短信验证码登录
  - 第三方OAuth登录(未来功能)
- **安全特性**:
  - 登录失败次数限制(5次锁定30分钟)
  - 设备指纹记录
  - 异地登录提醒
  - 图形验证码(失败3次后启用)

#### 1.3 密码管理
- **功能描述**: 安全的密码重置和修改机制
- **密码重置**:
  - 邮箱重置: 发送重置链接
  - 短信重置: 验证码 + 新密码
  - 安全问题重置(可选)
- **密码修改**:
  - 原密码验证 + 新密码
  - 强制密码策略检查
  - 修改后强制重新登录

### 2. JWT Token管理

#### 2.1 双Token策略
- **Access Token**:
  - 用途: API访问凭证
  - 生命周期: 75分钟
  - 载荷: userId, username, roles, permissions
  - 存储: 内存/请求头
- **Refresh Token**:
  - 用途: 刷新Access Token
  - 生命周期: 90天
  - 载荷: userId, tokenId(jti), deviceInfo
  - 存储: HttpOnly Cookie + Redis

#### 2.2 Token安全机制
- **Token签名**: RS256非对称加密
- **Token撤销**: 基于Redis的黑名单机制
- **设备绑定**: 限制同一账号设备数量(默认5个)
- **自动续期**: Access Token过期前自动刷新
- **强制失效**: 密码修改、账号冻结时清除所有token

#### 2.3 Token传播
- **Gateway验证**: 统一token验证和用户信息解析
- **服务传播**: 通过请求头传递用户上下文
- **跨服务调用**: 内部服务间token传递机制

### 3. 用户信息管理

#### 3.1 用户档案
- **基本信息**:
  - 个人资料: 昵称、头像、性别、生日
  - 联系方式: 邮箱、手机、地址
  - 偏好设置: 语言、时区、通知偏好
- **隐私设置**:
  - 档案可见性控制
  - 数据使用授权
  - 第三方分享设置

#### 3.2 账号安全
- **安全设置**:
  - 登录密码管理
  - 二次验证设置(TOTP)
  - 安全邮箱/手机绑定
  - 登录设备管理
- **安全日志**:
  - 登录历史记录
  - 敏感操作日志
  - 异常行为监控

### 4. 权限与角色管理

#### 4.1 角色体系
- **系统角色**:
  - ADMIN: 系统管理员
  - USER: 普通用户
  - VIP: 会员用户
  - PROVIDER: 服务提供商(预留)
- **权限粒度**:
  - 功能权限: 访问特定功能模块
  - 数据权限: 访问特定数据范围
  - 操作权限: 执行特定操作类型

#### 4.2 权限验证
- **接口级权限**: 基于注解的方法级权限控制
- **数据级权限**: 基于用户身份的数据访问控制
- **动态权限**: 支持运行时权限配置和检查

## API设计规范

### 认证相关API

```
POST   /auth/register          # 用户注册
POST   /auth/login             # 用户登录
POST   /auth/logout            # 用户登出
POST   /auth/refresh           # 刷新token
POST   /auth/forgot-password   # 忘记密码
POST   /auth/reset-password    # 重置密码
POST   /auth/change-password   # 修改密码
GET    /auth/verify-email      # 邮箱验证
POST   /auth/send-sms          # 发送短信验证码
```

### 用户信息API

```
GET    /users/profile          # 获取当前用户信息
PUT    /users/profile          # 更新用户信息
POST   /users/avatar           # 上传头像
GET    /users/security         # 获取安全设置
PUT    /users/security         # 更新安全设置
GET    /users/devices          # 获取登录设备列表
DELETE /users/devices/{deviceId}  # 移除登录设备
GET    /users/login-history    # 获取登录历史
```

### 权限管理API(管理员)

```
GET    /admin/users            # 获取用户列表
GET    /admin/users/{userId}   # 获取用户详情
PUT    /admin/users/{userId}/status    # 更新用户状态
POST   /admin/roles            # 创建角色
GET    /admin/roles            # 获取角色列表
PUT    /admin/roles/{roleId}   # 更新角色权限
```

## 数据模型设计

### 核心实体关系
```
User (1) ----< (N) UserRole
User (1) ----< (N) UserAuth  
User (1) ----< (N) LoginHistory
User (1) ----< (N) UserDevice
Role (1) ----< (N) UserRole
Role (1) ----< (N) RolePermission
```

### 用户表结构设计

#### users 表
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(50) NOT NULL,
    nickname VARCHAR(100),
    avatar_url VARCHAR(500),
    gender SMALLINT DEFAULT 0, -- 0:未知 1:男 2:女
    birthday DATE,
    status SMALLINT DEFAULT 1, -- 0:禁用 1:正常 2:待验证
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    last_login_at TIMESTAMP,
    last_login_ip INET,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### user_auth 表(多认证方式支持)
```sql
CREATE TABLE user_auth (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    auth_type VARCHAR(20) NOT NULL, -- email, phone, wechat, qq
    auth_key VARCHAR(100) NOT NULL, -- 认证标识
    auth_secret VARCHAR(255), -- 密码hash或token
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### refresh_tokens 表
```sql
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_id VARCHAR(100) UNIQUE NOT NULL, -- jti
    user_id BIGINT NOT NULL REFERENCES users(id),
    token_hash VARCHAR(255) NOT NULL,
    device_info JSONB,
    ip_address INET,
    user_agent TEXT,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 安全特性

#### 密码安全
- **加密算法**: BCrypt + 随机盐
- **密码策略**: 8-20位，必须包含数字和字母
- **历史密码**: 记录最近5次密码，防止重复使用
- **密码过期**: 90天强制更换(可配置)

#### Token安全
- **签名算法**: RS256非对称加密
- **密钥管理**: 定期轮换，支持多密钥验证
- **Token黑名单**: Redis存储已撤销的token
- **设备指纹**: 基于User-Agent + IP的设备识别

#### 防护机制
- **暴力破解防护**: 登录失败5次锁定30分钟
- **异地登录检测**: IP地址变化超过阈值时提醒
- **并发登录控制**: 同一账号最多5个设备同时在线
- **敏感操作验证**: 修改密码、绑定邮箱等需要二次验证

## 技术架构设计

### 服务架构
```
┌─────────────────┐    ┌─────────────────┐
│   Web Client    │    │  Mobile Client  │
└─────────┬───────┘    └─────────┬───────┘
          │                      │
          └──────────┬───────────┘
                     │
          ┌─────────────────┐
          │   API Gateway   │ (JWT验证 + 路由)
          └─────────┬───────┘
                    │
          ┌─────────────────┐
          │  User Service   │ (认证 + 用户管理)
          └─────────┬───────┘
                    │
    ┌───────────────┼───────────────┐
    │               │               │
┌───▼───┐   ┌───────▼───────┐   ┌───▼───┐
│ Redis │   │  PostgreSQL   │   │ Email │
│(Token)│   │   (UserDB)    │   │ SMS   │
└───────┘   └───────────────┘   └───────┘
```

### Gateway集成设计

#### JWT验证流程
```java
@Component
public class JwtAuthenticationFilter implements GlobalFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return validateJwtToken(exchange)
            .flatMap(claims -> {
                // 检查token黑名单
                if (isTokenBlacklisted(claims.getId())) {
                    return unauthorized();
                }
                
                // 添加用户上下文到请求头
                ServerHttpRequest modifiedRequest = exchange.getRequest()
                    .mutate()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-Username", claims.get("username", String.class))
                    .header("X-User-Roles", getUserRoles(claims))
                    .build();
                    
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            })
            .onErrorResume(error -> unauthorized());
    }
}
```

#### 路由配置
```yaml
spring:
  cloud:
    gateway:
      routes:
        # 认证相关路由（无需token验证）
        - id: auth-routes
          uri: lb://petify-user-service
          predicates:
            - Path=/api/user/auth/**
          filters:
            - StripPrefix=2
            
        # 用户信息路由（需要token验证）
        - id: user-routes
          uri: lb://petify-user-service
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=2
            - JwtAuthenticationFilter
```

### Service内部设计

#### 认证服务架构
```java
@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        // 1. 验证用户凭据
        User user = authenticateUser(request);
        
        // 2. 检查账号状态
        validateUserStatus(user);
        
        // 3. 生成token对
        String accessToken = jwtTokenUtil.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user, httpRequest);
        
        // 4. 记录登录日志
        recordLoginHistory(user, httpRequest);
        
        // 5. 清理过期的refresh token
        refreshTokenService.cleanExpiredTokens(user.getId());
        
        return new LoginResponse(accessToken, refreshToken);
    }
    
    public RefreshResponse refresh(String refreshToken, HttpServletRequest request) {
        // 1. 验证refresh token
        RefreshTokenEntity tokenEntity = refreshTokenService.validateRefreshToken(refreshToken);
        
        // 2. 检查设备一致性
        validateDeviceConsistency(tokenEntity, request);
        
        // 3. 生成新的access token
        User user = userRepository.findById(tokenEntity.getUserId())
            .orElseThrow(() -> new UserNotFoundException("用户不存在"));
        String newAccessToken = jwtTokenUtil.generateAccessToken(user);
        
        // 4. 更新refresh token使用时间
        refreshTokenService.updateLastUsed(tokenEntity.getId());
        
        return new RefreshResponse(newAccessToken);
    }
}
```

#### 权限验证架构
```java
@Component
public class PermissionEvaluator {
    
    @Autowired
    private UserRoleService userRoleService;
    
    public boolean hasPermission(Long userId, String resource, String action) {
        List<String> userPermissions = userRoleService.getUserPermissions(userId);
        String requiredPermission = resource + ":" + action;
        return userPermissions.contains(requiredPermission) || 
               userPermissions.contains(resource + ":*") ||
               userPermissions.contains("*:*");
    }
    
    public boolean canAccessPet(Long userId, Long petId) {
        // 检查用户是否有权限访问特定宠物
        return petService.isPetOwner(userId, petId) || 
               hasPermission(userId, "pet", "admin");
    }
}
```

## 配置管理

### JWT配置
```yaml
jwt:
  # 密钥配置
  private-key: classpath:keys/jwt-private.pem
  public-key: classpath:keys/jwt-public.pem
  
  # Token配置
  access-token:
    expiration: 4500        # 75分钟
    issuer: petify-platform
  
  refresh-token:
    expiration: 7776000     # 90天
    max-devices: 5          # 最大设备数
  
  # 安全配置
  token-blacklist:
    cleanup-interval: 3600  # 黑名单清理间隔(秒)
```

### 安全配置
```yaml
security:
  # 密码策略
  password:
    min-length: 8
    max-length: 20
    require-digit: true
    require-letter: true
    require-special: false
    history-count: 5        # 记录历史密码数量
    
  # 登录安全
  login:
    max-attempts: 5         # 最大尝试次数
    lockout-duration: 1800  # 锁定时长(秒)
    captcha-threshold: 3    # 验证码触发阈值
    
  # 设备管理
  device:
    max-concurrent: 5       # 最大并发设备
    trust-duration: 2592000 # 设备信任期(30天)
```

## 部署与监控

### 健康检查
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
  endpoint:
    health:
      show-details: always
  health:
    redis:
      enabled: true
    db:
      enabled: true
```

### 日志配置
```yaml
logging:
  level:
    com.petify.user.security: DEBUG
    org.springframework.security: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{traceId}] %logger{36} - %msg%n"
```

## 实现优先级

### Phase 1 - 核心认证功能
1. **基础认证**: 注册、登录、JWT生成
2. **Token管理**: 双token策略、刷新机制
3. **Gateway集成**: JWT验证、用户信息传递
4. **基础安全**: 密码加密、登录限制

### Phase 2 - 完善功能
1. **用户管理**: 用户信息CRUD、头像上传
2. **安全增强**: 设备管理、登录历史
3. **权限系统**: 角色权限、接口权限控制
4. **密码安全**: 找回密码、修改密码

### Phase 3 - 高级功能
1. **多因子认证**: TOTP、短信验证
2. **第三方登录**: OAuth2集成
3. **风险控制**: 异地登录检测、行为分析
4. **管理功能**: 用户管理后台、统计报表

## 成功指标

### 功能指标
- 注册成功率 > 95%
- 登录响应时间 < 200ms
- Token验证成功率 > 99.9%

### 安全指标
- 暴力破解防护有效性 > 99%
- 未授权访问阻止率 = 100%
- 敏感数据泄露事件 = 0

### 用户体验指标
- 登录便利性评分 > 4.5/5.0
- 密码找回成功率 > 90%
- 用户投诉率 < 1%