# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Petify is a microservices-based pet management platform built with Spring Cloud Alibaba. The architecture follows a distributed design with:

- **API Gateway** (petify-gateway:8080) - Single entry point using Spring Cloud Gateway
- **User Service** (petify-user-service:8081) - Authentication, user management, JWT tokens
- **Pet Service** (petify-pet-service:8082) - Pet information, categories, medical records
- **Appointment Service** (petify-appointment-service:8083) - Booking, providers, reviews
- **Common Module** (petify-common) - Shared utilities and configurations

All services share a single PostgreSQL database to reduce resource usage while maintaining logical separation through table prefixes.

## Key Technologies

- **Spring Boot 3.2.5** with Java 17
- **Spring Cloud Alibaba 2022.0.0.0** with Nacos for service discovery/config
- **PostgreSQL 15** with shared database for all services
- **Redis 7.0** for caching
- **MyBatis Plus 3.5.5** for data access
- **Docker & Docker Compose** for containerization

## Java 17 Modern Features

项目使用Java 17，强烈建议充分利用现代Java特性提升代码质量：

- **Records** - 用于DTO、实体类和不可变数据对象
- **Pattern Matching** - 简化instanceof检查和switch表达式
- **Text Blocks** - 改善SQL查询、JSON字符串和HTML模板的可读性
- **Sealed Classes** - 增强类型安全，限制继承层次
- **Switch Expressions** - 替代传统switch语句，支持yield返回值
- **Local Variable Type Inference (var)** - 减少样板代码，提高可读性
- **Enhanced instanceof** - 模式匹配简化类型检查和转换
- **Helpful NullPointerExceptions** - 更精确的空指针异常信息

## Development Commands

### Build and Package
```bash
# Build all modules
mvn clean package -DskipTests

# Build specific service
mvn clean package -DskipTests -pl petify-gateway

# Build with Docker images
mvn clean package docker:build -DskipTests
```

### Local Development with Docker Compose Support

**Spring Boot Docker Compose自动管理 (推荐):**
```bash
# 直接启动任意服务，Spring Boot会自动启动依赖的Docker服务
mvn spring-boot:run -pl petify-user-service
mvn spring-boot:run -pl petify-pet-service  
mvn spring-boot:run -pl petify-appointment-service

# 或在IDE中直接运行Application主类
```

**传统手动管理:**
```bash
# Start infrastructure services first (required order)
docker-compose up -d nacos nacos-mysql postgres redis

# Start all services
docker-compose up -d

# Start specific services
docker-compose up -d gateway user-service pet-service appointment-service
```

### Service Management
```bash
# Check service status
docker-compose ps

# View logs for specific service
docker-compose logs -f gateway
docker-compose logs -f user-service

# Restart a service
docker-compose restart gateway
```

### Database Access
```bash
# Connect to shared database
docker exec -it petify-postgres psql -U petify -d petify
```

## Architecture Patterns

### Service Communication
- Services register with Nacos at startup
- Inter-service calls use service names (e.g., `petify-user-service`) resolved via Nacos
- Gateway routes external requests based on path prefixes (`/api/user/**`, `/api/pet/**`, `/api/appointment/**`)

### Database Strategy
- All services share a single PostgreSQL database
- Services maintain logical boundaries through application-level separation
- Foreign key relationships span across service domains
- Database initialization script in `docker/postgres/init-all.sql`

### Configuration Management

**配置分层最佳实践** - 基于Spring Boot 2.4+和Spring Cloud Alibaba推荐：

#### 配置文件结构
1. **`application.yml`** - 主配置文件，包含：
   - 基础应用配置（服务名、端口、Docker Compose）
   - Nacos连接配置（服务注册与发现、配置中心）
   - `spring.config.import` 配置，使用`optional:`前缀优雅降级

2. **`application-{profile}.yml`** - 环境特定fallback配置：
   - 完整的业务配置作为本地备选方案
   - 当Nacos不可用时确保服务正常启动
   - 支持本地开发环境的独立性

#### 配置加载优先级
- **Nacos远程配置** > 本地`application-{profile}.yml` > `application.yml`
- 使用`optional:nacos:`确保配置导入失败时不影响启动
- 符合Spring Boot配置外部化最佳实践

#### 示例配置
```yaml
# application.yml
spring:
  config:
    import:
      - optional:nacos:petify-gateway-dev.yml?group=DEFAULT_GROUP&refresh=true
      - optional:nacos:common-config.yml?group=DEFAULT_GROUP&refresh=true
```

#### 环境配置
- Nacos namespace `petify` 隔离环境配置
- 共享配置可定义为 `common-config.yml`
- 支持动态配置刷新（`refresh=true`）

### Gateway Routing
- Paths are stripped by 2 levels (e.g., `/api/user/login` → `/login`)
- Load balancing handled automatically by Spring Cloud LoadBalancer
- CORS configured globally for all routes

## Service Startup Dependencies

Critical startup order:
1. Infrastructure: `nacos`, `nacos-mysql`, `postgres`, `redis`
2. Gateway: Requires Nacos and Redis
3. Business Services: Require Nacos, shared PostgreSQL, and Redis

## Port Allocation

- Gateway: 8080
- User Service: 8081  
- Pet Service: 8082
- Appointment Service: 8083
- Nacos: 8848
- PostgreSQL: 5432 (shared database)
- Redis: 6379

## Monitoring

- Nacos Console: http://localhost:8848/nacos (nacos/nacos)
- Service health: http://localhost:808x/actuator/health
- All actuator endpoints enabled for monitoring

## Adding New Services

1. Create Maven module under parent POM
2. Add Nacos discovery and config dependencies
3. Configure bootstrap.yml with service name and Nacos settings
4. Add route configuration to gateway's bootstrap.yml
5. Create PostgreSQL database and initialization script
6. Add service to docker-compose.yml with proper dependencies

## Database Schema Overview

### Shared Database (petify:5432)

**User Domain Tables:**
- `users`: Core user information with authentication fields
- `user_roles`: Role-based access control mapping
- `user_auth`: Multiple authentication method support

**Pet Domain Tables:**
- `pet_categories`: Hierarchical category structure with parent-child relationships
- `pet_breeds`: Breed characteristics, lifespan, size information
- `pets`: Pet information linked to users via owner_id (FK to users table)
- `pet_vaccinations`: Vaccination scheduling and history
- `pet_medical_records`: Complete medical history tracking

**Appointment Domain Tables:**
- `service_items`: Available service types (medical, grooming, training, boarding)
- `service_providers`: Clinics, hospitals, service facilities
- `provider_staff`: Individual service providers with specializations
- `appointment_slots`: Time slot availability management
- `appointments`: Booking records with FK to users and pets tables
- `appointment_status_history`: Complete audit trail of status changes
- `service_reviews`: Customer feedback and rating system

## Key Implementation Notes

### Cross-Service Data Access
- Services share a single database but maintain logical boundaries
- Foreign key relationships exist across service domains (e.g., pets.owner_id → users.id)
- Services can directly access related data but should respect domain ownership
- Consider using service APIs for complex cross-domain operations

### Authentication Architecture
- **JWT双Token策略**: Access Token (75分钟) + Refresh Token (90天)
- **Gateway统一认证**: JWT验证在API Gateway层统一处理
- **用户服务职责**: 用户注册/登录、Token生成和刷新、用户信息管理
- **安全特性**: Token黑名单、设备管理、登录失败锁定、密码强度验证
- **权限控制**: 基于角色的访问控制(RBAC)，支持接口级和数据级权限
- **服务间通信**: Gateway验证JWT后通过请求头传递用户上下文(X-User-Id, X-Username等)
- **Token存储**: Access Token存内存，Refresh Token存Redis + 数据库
- **密钥管理**: RS256非对称加密，支持密钥轮换

### Testing Status
- No automated tests currently implemented
- Relies on Actuator health endpoints for basic health checks
- Manual testing through service endpoints

### Docker Compose Support
- Spring Boot 3.1+支持自动Docker Compose管理
- 项目根目录的`compose.yaml`定义依赖服务
- 启动任意微服务时会自动启动依赖的Docker服务
- 应用停止时会自动停止Docker服务
- 无需手动执行`docker-compose up`命令

### Configuration Hierarchy
1. Bootstrap configuration (`bootstrap.yml`) - service registration
2. Nacos shared config (`common-config.yml`) - shared settings
3. Service-specific Nacos config - individual service settings
4. Environment variables - runtime overrides for Docker deployment

## Documentation Structure

### Product Requirements
- `docs/product-requirements/user-service.md` - User Service产品需求和功能规划
- `docs/product-requirements/pet-service.md` - Pet Service产品需求和功能规划
- `docs/product-requirements/appointment-service.md` - Appointment Service产品需求和功能规划

### Technical Design
- `docs/technical-design/authentication-architecture.md` - JWT认证架构技术设计
- `docs/technical-design/database-design.md` - 数据库设计和优化方案
- `docs/technical-design/api-specification.md` - API接口规范和设计原则

## Implementation Priority

基于依赖关系，推荐的实现顺序：
1. **User Service** - 认证基础，其他服务的依赖
2. **Pet Service** - 宠物管理，用户的核心功能
3. **Appointment Service** - 预约功能，依赖用户和宠物数据

## Git Commit Guidelines

**小步骤提交原则** - 这是必须遵守的开发规范：

- **每个功能修改都必须单独提交** - 修改一个功能立即提交一次
- **提交粒度要细** - 一个commit只包含一个具体的功能点或修复
- **及时提交** - 完成任何可独立运行的功能后立即commit
- **提交信息要清晰** - 简洁描述本次修改的具体内容和目的
- **避免大批量提交** - 不要积累多个功能修改后一次性提交

示例提交规范：
```bash
# 好的提交示例
git commit -m "Add user registration endpoint with validation"
git commit -m "Implement JWT token generation service"
git commit -m "Add password strength validation rules"

# 避免的提交示例
git commit -m "Complete user service implementation"  # 过于宽泛
git commit -m "Fix bugs and add features"             # 不明确
```

这种小步骤提交方式有助于：
- 更好的代码版本控制和回滚
- 清晰的开发进度追踪
- 便于代码审查和问题定位
- 团队协作时减少合并冲突