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
- Bootstrap configuration in each service's `bootstrap.yml`
- Nacos namespace `petify` isolates this environment
- Shared configurations can be defined in Nacos as `common-config.yml`

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
- JWT utilities available in petify-common module
- User authentication handled by user service
- No gateway-level authentication currently implemented
- Service-to-service authentication not implemented

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