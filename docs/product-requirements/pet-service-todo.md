# Pet Service Implementation TODO List

## Current Status
- **Implementation Progress**: 0% (只有基础框架和配置)
- **Database Schema**: ✅ 完成 (5张宠物相关表)
- **Infrastructure**: ✅ 完成 (Nacos、PostgreSQL、Redis、MyBatis Plus)
- **Gateway Routing**: ✅ 完成 (`/api/pet/**`)

## Phase 1 - MVP核心功能 (高优先级)

### 1. 基础架构搭建
- [ ] 创建所有宠物相关实体类 (Entity)
  - [ ] PetCategory 实体
  - [ ] PetBreed 实体 
  - [ ] Pet 实体
  - [ ] PetVaccination 实体
  - [ ] PetMedicalRecord 实体
- [ ] 创建 MyBatis Plus Mapper 接口
- [ ] 创建请求/响应 DTO 类
- [ ] 配置全局异常处理

### 2. 宠物管理CRUD
- [ ] `POST /pets` - 注册新宠物
- [ ] `GET /pets` - 获取用户宠物列表
- [ ] `GET /pets/{petId}` - 获取宠物详情
- [ ] `PUT /pets/{petId}` - 更新宠物信息
- [ ] `DELETE /pets/{petId}` - 删除宠物档案

### 3. 品种和分类查询
- [ ] `GET /categories` - 获取宠物分类树
- [ ] `GET /categories/{categoryId}/breeds` - 按分类获取品种
- [ ] `GET /breeds` - 搜索品种（支持关键词）
- [ ] `GET /breeds/{breedId}` - 获取品种详情

### 4. 基础疫苗记录管理
- [ ] `GET /pets/{petId}/vaccinations` - 获取疫苗记录
- [ ] `POST /pets/{petId}/vaccinations` - 添加疫苗记录
- [ ] `PUT /pets/{petId}/vaccinations/{vaccinationId}` - 更新疫苗记录
- [ ] `DELETE /pets/{petId}/vaccinations/{vaccinationId}` - 删除疫苗记录

### 5. 用户认证集成
- [ ] JWT Token 验证
- [ ] 用户宠物所有权验证
- [ ] 权限控制（防止用户访问他人宠物）

## Phase 2 - 增强功能 (中优先级)

### 1. 完整健康档案管理
- [ ] `GET /pets/{petId}/medical-records` - 获取医疗记录
- [ ] `POST /pets/{petId}/medical-records` - 添加医疗记录
- [ ] `PUT /pets/{petId}/medical-records/{recordId}` - 更新医疗记录
- [ ] `DELETE /pets/{petId}/medical-records/{recordId}` - 删除医疗记录

### 2. 照片管理
- [ ] `POST /pets/{petId}/photos` - 上传宠物照片
- [ ] `GET /pets/{petId}/photos` - 获取宠物照片
- [ ] `PUT /pets/{petId}/avatar` - 设置宠物头像
- [ ] `DELETE /pets/{petId}/photos/{photoId}` - 删除照片

### 3. 健康提醒系统
- [ ] `GET /pets/{petId}/health-reminders` - 获取健康提醒
- [ ] `POST /pets/{petId}/health-reminders` - 创建健康提醒
- [ ] `PUT /pets/{petId}/health-reminders/{reminderId}` - 更新提醒

### 4. 服务集成
- [ ] 与 Appointment Service 集成
- [ ] 基于宠物特征的服务推荐

## Phase 3 - 高级功能 (低优先级)

### 1. 智能推荐系统
- [ ] `POST /breeds/recommend` - 基于特征的品种推荐
- [ ] 基于宠物信息的服务推荐

### 2. 健康分析
- [ ] 基于医疗记录的健康趋势分析
- [ ] 疫苗接种提醒智能化

### 3. 社交功能
- [ ] 宠物社区
- [ ] 经验分享

## 技术实现要求

### 数据访问层
- **技术栈**: Spring Data JPA with Hibernate (与User Service保持一致)
- **连接池**: Druid
- **实体映射**: 5张宠物相关表的JPA实体类
- **Repository**: JpaRepository 接口实现
- **DTO**: 完整的请求/响应 DTO
- **迁移原因**: MyBatis Plus与Spring Boot 3.2.5+兼容性问题

### 服务层
- **服务接口**: 定义服务契约
- **服务实现**: 业务逻辑实现
- **事务管理**: Spring 声明式事务
- **缓存**: Redis 集成

### 控制器层
- **REST 控制器**: 实现所有 API 端点
- **请求验证**: 输入验证和错误处理
- **响应格式**: 使用通用 Result 包装器
- **异常处理**: 全局异常处理

### 安全集成
- **JWT 验证**: 与 Gateway JWT 认证集成
- **用户上下文**: 从请求头提取用户信息
- **授权**: 验证宠物所有权

## 跨服务依赖

### User Service 集成
- 通过 `owner_id` 外键验证用户存在
- 从 Gateway JWT Token 提取用户上下文

### Appointment Service 集成（未来）
- 为预约服务提供宠物信息
- 通过 `pet_id` 查询预约历史

## 实现建议
1. **从 Phase 1 开始**：优先实现基础 CRUD 功能
2. **小步骤提交**：每完成一个功能点立即提交
3. **测试驱动**：每个功能都要有对应的测试验证
4. **安全优先**：确保用户只能访问自己的宠物数据
5. **性能考虑**：合理使用 Redis 缓存提升查询性能