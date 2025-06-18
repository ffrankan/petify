# 数据访问层技术设计

## 概述

Petify项目采用混合数据访问策略，结合Spring Data JPA和MyBatis Plus的优势，为不同服务提供最适合的数据访问解决方案。

## 技术选型策略

### User Service - Spring Data JPA
- **技术栈**: Spring Data JPA + Hibernate
- **选型原因**:
  - 与Spring Boot 3.2.5完美兼容
  - 类型安全的查询API
  - 自动化schema验证
  - 优秀的IDE支持和代码提示
  - 标准JPA规范，便于团队协作

### Pet & Appointment Services - MyBatis Plus
- **技术栈**: MyBatis Plus 3.5.10.1 + MyBatis Spring 3.0.4
- **选型原因**:
  - 灵活的SQL控制能力
  - 复杂查询性能优化
  - 与遗留系统更好的兼容性
  - 丰富的代码生成工具

## Migration History

### User Service MyBatis Plus → JPA 迁移

#### 迁移背景
- **问题**: MyBatis Plus 3.5.5与Spring Boot 3.2.5存在兼容性问题
- **错误**: `Invalid value type for attribute 'factoryBeanObjectType': java.lang.String`
- **根本原因**: Spring 6.1版本对FactoryBean类型检查更加严格

#### 解决方案
1. **依赖升级**: 
   - 移除MyBatis Plus依赖
   - 添加Spring Data JPA依赖
   
2. **代码重构**:
   - Entity类：MyBatis Plus注解 → JPA注解
   - 数据访问：BaseMapper → JpaRepository
   - 服务层：更新所有数据访问调用

3. **配置调整**:
   - 移除MyBatis Plus配置
   - 添加JPA/Hibernate配置

#### 迁移对比

**Before (MyBatis Plus)**:
```java
@TableName("users")
public class User {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("username")
    private String username;
}

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

**After (JPA)**:
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", unique = true, nullable = false)
    private String username;
}

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

## 配置管理

### JPA配置 (User Service)
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### MyBatis Plus配置 (Pet/Appointment Services)
```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

## 版本兼容性矩阵

| 组件 | User Service | Pet/Appointment Services |
|------|-------------|-------------------------|
| Spring Boot | 3.2.5 | 3.2.5 |
| Data Access | Spring Data JPA | MyBatis Plus 3.5.10.1 |
| ORM | Hibernate 6.4.4 | MyBatis 3.5.x |
| Connection Pool | HikariCP | Druid |

## 最佳实践

### 通用原则
1. **Repository模式**: 所有服务都使用Repository模式封装数据访问
2. **事务管理**: 使用Spring声明式事务管理
3. **连接池**: 统一使用HikariCP（JPA）或Druid（MyBatis Plus）
4. **SQL审计**: 开发环境启用SQL日志记录

### JPA最佳实践
1. **实体设计**: 使用JPA注解明确映射关系
2. **查询优化**: 使用@Query注解自定义查询
3. **懒加载**: 合理使用FetchType控制关联数据加载
4. **批量操作**: 使用JPA的批量操作API

### MyBatis Plus最佳实践
1. **代码生成**: 使用MyBatis Plus Generator生成基础代码
2. **条件构造**: 使用LambdaQueryWrapper构建类型安全查询
3. **分页插件**: 启用MyBatis Plus内置分页插件
4. **性能监控**: 使用p6spy监控SQL执行性能

## 故障排除

### 常见问题
1. **兼容性问题**: 确保MyBatis Plus版本与Spring Boot版本兼容
2. **Schema验证**: JPA的ddl-auto设置需要与数据库schema保持一致
3. **类型映射**: 注意Java类型与数据库字段类型的映射关系

### 调试技巧
1. **SQL日志**: 启用SQL日志查看实际执行的SQL语句
2. **连接池监控**: 监控数据库连接池状态
3. **事务边界**: 使用Spring事务日志追踪事务边界

## 未来规划

1. **统一化**: 考虑将所有服务逐步迁移到Spring Data JPA
2. **性能优化**: 引入Redis缓存减少数据库访问
3. **分库分表**: 根据业务增长考虑数据库分片策略
4. **读写分离**: 实现主从数据库读写分离