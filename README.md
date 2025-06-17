# Petify - 宠物管理微服务平台

基于Spring Cloud Alibaba的宠物管理微服务平台，使用PostgreSQL数据库。

## 技术架构

- **Spring Boot**: 3.2.5
- **Spring Cloud**: 2023.0.1
- **Spring Cloud Alibaba**: 2022.0.0.0
- **数据库**: PostgreSQL 15
- **缓存**: Redis 7.0
- **注册中心**: Nacos
- **配置中心**: Nacos
- **API网关**: Spring Cloud Gateway
- **容器化**: Docker & Docker Compose

## 项目结构

```
petify/
├── pom.xml                          # 父POM文件
├── docker-compose.yml               # Docker编排文件
├── docker/                          # Docker相关配置
│   ├── nacos/
│   │   └── mysql/
│   │       └── nacos-mysql.sql     # Nacos数据库初始化脚本
│   └── postgres/
│       ├── init-user.sql           # 用户服务数据库初始化
│       ├── init-pet.sql            # 宠物服务数据库初始化
│       └── init-appointment.sql    # 预约服务数据库初始化
├── petify-gateway/                  # API网关服务
├── petify-user-service/             # 用户管理服务
├── petify-pet-service/              # 宠物信息服务
├── petify-appointment-service/      # 预约管理服务
└── petify-common/                   # 公共模块
```

## 服务说明

### 1. API网关 (petify-gateway:8080)
- 统一入口，路由转发
- 负载均衡
- 跨域处理
- 请求限流

### 2. 用户服务 (petify-user-service:8081)
- 用户注册、登录、认证
- 用户信息管理
- 角色权限管理
- JWT令牌管理

### 3. 宠物服务 (petify-pet-service:8082)
- 宠物信息管理
- 宠物分类和品种管理
- 疫苗记录管理
- 医疗记录管理

### 4. 预约服务 (petify-appointment-service:8083)
- 预约时间管理
- 服务项目管理
- 服务提供者管理
- 预约状态跟踪
- 服务评价管理

## 数据库设计

### 用户服务数据库 (petify_user)
- `users`: 用户基本信息
- `user_roles`: 用户角色
- `user_auth`: 用户认证信息

### 宠物服务数据库 (petify_pet)
- `pet_categories`: 宠物分类
- `pet_breeds`: 宠物品种
- `pets`: 宠物信息
- `pet_vaccinations`: 疫苗记录
- `pet_medical_records`: 医疗记录

### 预约服务数据库 (petify_appointment)
- `service_items`: 服务项目
- `service_providers`: 服务提供者
- `provider_staff`: 服务人员
- `appointment_slots`: 预约时间段
- `appointments`: 预约记录
- `appointment_status_history`: 预约状态历史
- `service_reviews`: 服务评价

## 快速启动

### 1. 环境要求
- JDK 17+
- Maven 3.8+
- Docker & Docker Compose

### 2. 启动基础服务
```bash
# 启动所有基础服务 (Nacos, PostgreSQL, Redis)
docker-compose up -d nacos nacos-mysql postgres-user postgres-pet postgres-appointment redis

# 等待服务启动完成，访问Nacos控制台
# http://localhost:8848/nacos (用户名/密码: nacos/nacos)
```

### 3. 构建并启动微服务
```bash
# 构建项目
mvn clean package -DskipTests

# 启动所有服务
docker-compose up -d

# 或者逐个启动服务
docker-compose up -d gateway
docker-compose up -d user-service
docker-compose up -d pet-service
docker-compose up -d appointment-service
```

### 4. 验证服务状态
```bash
# 检查所有服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f gateway
docker-compose logs -f user-service
```

## API网关路由规则

| 服务 | 路径前缀 | 目标服务 | 端口 |
|------|----------|----------|------|
| 用户服务 | `/api/user/**` | petify-user-service | 8081 |
| 宠物服务 | `/api/pet/**` | petify-pet-service | 8082 |
| 预约服务 | `/api/appointment/**` | petify-appointment-service | 8083 |

## 端口分配

| 服务 | 端口 |
|------|------|
| API网关 | 8080 |
| 用户服务 | 8081 |
| 宠物服务 | 8082 |
| 预约服务 | 8083 |
| Nacos | 8848 |
| Nacos MySQL | 3307 |
| PostgreSQL (用户) | 5432 |
| PostgreSQL (宠物) | 5433 |
| PostgreSQL (预约) | 5434 |
| Redis | 6379 |

## 开发指南

### 1. 添加新的微服务
1. 创建新的Maven模块
2. 配置POM依赖
3. 实现服务逻辑
4. 配置Nacos注册
5. 更新网关路由
6. 添加Docker配置

### 2. 数据库操作
```bash
# 连接用户服务数据库
docker exec -it petify-postgres-user psql -U petify -d petify_user

# 连接宠物服务数据库
docker exec -it petify-postgres-pet psql -U petify -d petify_pet

# 连接预约服务数据库
docker exec -it petify-postgres-appointment psql -U petify -d petify_appointment
```

### 3. 监控和运维
- Nacos控制台: http://localhost:8848/nacos
- 各服务健康检查: http://localhost:808x/actuator/health
- 服务发现状态: 在Nacos控制台查看服务列表

## 注意事项

1. **服务启动顺序**: 先启动基础服务(Nacos、数据库、Redis)，再启动业务服务
2. **数据库连接**: 各服务使用独立的PostgreSQL数据库，避免服务间耦合
3. **配置管理**: 所有配置统一通过Nacos配置中心管理
4. **服务通信**: 服务间通过Nacos进行服务发现和负载均衡
5. **数据一致性**: 跨服务数据操作需要考虑分布式事务处理

## 许可证

MIT License