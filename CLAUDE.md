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
- **User Module**: Registration, login (Token + Redis + ThreadLocal), user info management
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
├── config/          # Spring configurations (Redis, CORS, ThreadPool)
├── controller/      # REST API endpoints
├── service/         # Business logic
├── mapper/          # MyBatis Plus mappers
├── entity/          # Database entities
├── dto/             # Data Transfer Objects (input validation)
├── vo/              # View Objects (output serialization)
├── common/          # Shared classes (Result, ResultCode, PageResult)
├── exception/       # Custom exceptions and global handler
├── interceptor/     # Request interceptors (LoginInterceptor, RefreshInterceptor)
├── annotation/      # Custom annotations (AccessLimit, RateLimit)
├── aspect/          # AOP aspects
├── mq/              # RocketMQ producers/consumers
├── util/            # Utility classes (UserHolder, CookieUtil, etc.)
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
- Redis key namespace: `galen:token:{token}` for user session data
- Druid monitoring available at `/druid` (admin/admin123)

### Token + Redis + ThreadLocal Authentication

**Overview**: Token-based stateless authentication using Redis for storage and ThreadLocal for request-scoped user context.

**Components**:
- **UserHolder**: ThreadLocal-based utility for storing/retrieving User objects within a request scope
- **RefreshInterceptor** (order=1): Extracts token from request header, fetches user info from Redis Hash, stores in UserHolder, refreshes token expiration time. Intercepts **all paths**.
- **LoginInterceptor** (order=2): Validates user presence in UserHolder, returns 401 if not logged in. Excludes `/user/login`, `/user/register`, `/user/logout`, etc.

**Redis Storage Format**:
```
Key: galen:token:{token}
Type: Hash
Fields: id, username, phone, email, nickname, etc.
TTL: 30 minutes (refreshed on each request)
```

**Authentication Flow**:
1. Login: Generate UUID token, store user info as Hash in Redis, return token
2. Request: RefreshInterceptor extracts token from `Authorization` or `token` header, fetches user from Redis, stores in ThreadLocal
3. Controller: Access user via `UserHolder.getUser()` or `UserHolder.getUserId()`
4. Logout: Delete Redis key for token
5. Request End: LoginInterceptor cleans up ThreadLocal in `afterCompletion()`

**Usage in Controllers**:
```java
@GetMapping("/info")
public Result<UserVO> getUserInfo() {
    User user = UserHolder.getUser();
    if (user == null) {
        return Result.error(401, "请先登录");
    }
    // Use user object
}
```

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
- No logical delete (physical delete only)

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