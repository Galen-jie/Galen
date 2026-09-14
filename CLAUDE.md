# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Galen is a distributed seckill (flash sale) system built with Spring Boot. It handles high-concurrency scenarios with features like user authentication, product management, seckill ordering, and order processing.

**Tech Stack**: Spring Boot 3.1.5, JDK 17, MySQL 8.0, Redis, RocketMQ, MyBatis Plus, Redisson

## Build and Run Commands

```bash
# Build project
mvn clean install

# Run application (development mode)
mvn spring-boot:run

# Run tests
mvn test

# Run single test class
mvn test -Dtest=ClassName

# Package
mvn clean package -DskipTests
```

## Architecture

### Module Structure
- **User Module**: Registration, login (Session + Redis), user info management
- **Product Module**: Product display, inventory management
- **Seckill Module**: Core seckill logic with multi-level rate limiting, hidden paths, CAPTCHA validation, Redis atomic stock reduction, RocketMQ async processing
- **Order Module**: Order creation, payment simulation, timeout handling

### Key Design Patterns
- **Anti-Overselling**: 3-layer protection (Redis Lua scripts, DB optimistic locks, distributed locks via Redisson)
- **Anti-Scraping**: CAPTCHA, hidden seckill paths, rate limiting annotations, IP/user-level limits
- **High Concurrency**: Async processing via RocketMQ, multi-level caching (Caffeine + Redis), cache preheating
- **Consistency**: RocketMQ transaction messages, eventual consistency

### Package Structure
```
com.galen.seckill
├── config/          # Spring configurations (Redis, Session, CORS, ThreadPool)
├── controller/      # REST API endpoints
├── service/         # Business logic
├── mapper/          # MyBatis Plus mappers
├── entity/          # Database entities
├── dto/             # Data Transfer Objects (input validation)
├── vo/              # View Objects (output serialization)
├── common/          # Shared classes (Result, ResultCode, PageResult)
├── exception/       # Custom exceptions and global handler
├── interceptor/     # Request interceptors (LoginInterceptor)
├── annotation/      # Custom annotations (AccessLimit, RateLimit)
├── aspect/          # AOP aspects
├── mq/              # RocketMQ producers/consumers
├── util/            # Utility classes
└── task/            # Scheduled tasks
```

## Database Setup

Database schema located at `docs/sql/schema.sql`. Initialize with:

```bash
# Run SQL initialization
mysql -u root -p < docs/sql/schema.sql
```

**Key Tables**:
- `user`: User accounts with BCrypt passwords
- `seckill_goods`: Seckill products with stock, time windows
- `seckill_order`: Orders with unique constraint on (user_id, seckill_id, goods_id) to prevent duplicate purchases

## Development Conventions

### Configuration
- Environment-specific configs: `application-dev.yml` (local), `application-prod.yml` (production)
- Redis stores Session data with namespace `galen:session`
- Druid monitoring available at `/druid` (admin/admin123)

### API Response Format
All APIs return `Result<T>` with structure:
```json
{
  "code": 200,
  "message": "Success",
  "data": {},
  "timestamp": 1234567890
}
```

Use `ResultCode` enum for standard response codes.

### Exception Handling
- Throw `BusinessException` for business logic errors
- Throw `SeckillException` for seckill-specific errors
- Global exception handler automatically catches and converts to `Result` responses

### Validation
- Use Jakarta Validation annotations in DTOs
- Phone format: `^1[3-9]\d{9}$`
- Password: 8-20 chars with uppercase, lowercase, and digits

### Database Operations
- MyBatis Plus provides base CRUD operations
- Custom queries in `src/main/resources/mapper/*.xml`
- Logical delete enabled (field: `deleted`)

## Seckill Flow

1. User requests seckill path with CAPTCHA validation
2. Path is generated and cached in Redis (60s expiry)
3. User submits seckill request with dynamic path
4. Rate limiting checks (user-level, IP-level)
5. Redis atomic stock reduction via Lua script
6. Message sent to RocketMQ queue
7. Consumer processes: creates order, deducts DB stock
8. User polls for result status

## Testing Data

Test users in database (password: `123456`):
- admin / 13800138000
- test1 / 13800138001
- test2 / 13800138002