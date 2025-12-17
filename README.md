# Gym Health Tech

Ứng dụng backend Spring Boot cho hệ thống quản lý bài tập, kế hoạch dinh dưỡng và tính năng xã hội (bình luận, bài viết, notification).

Mục tiêu của README này:
- Giới thiệu nhanh dự án
- Hướng dẫn cài đặt & chạy (local & Docker)
- Liệt kê biến môi trường quan trọng
-----

## Nội dung chính
- Ngôn ngữ & công nghệ: Java 17, Spring Boot (3.x), Spring Data JPA, PostgreSQL (pgvector), Redis, Flyway, MapStruct, Lombok
- Build: Maven (wrapper `mvnw`/`mvnw.cmd`)
- Docker: `docker-compose.yml` kèm service `postgres` và `redis`

-----

## Yêu cầu
- Java 17
- Maven (hoặc dùng `mvnw.cmd` trên Windows)
- Docker & Docker Compose (nếu chạy bằng container)

-----

## Biến môi trường quan trọng
Bạn có thể đặt các biến này trong một file `.env` (đặt cạnh `docker-compose.yml`) hoặc export trong môi trường của OS.
Ví dụ (không dùng giá trị production):

```
# PostgreSQL
POSTGRES_SUPERUSER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=gym
POSTGRES_PORT=5432
POSTGRES_URL_DEV=jdbc:postgresql://localhost:5432/gym
APP_DB_USER_DEV=postgres
APP_DB_PASSWORD_DEV=postgres

# Redis
REDIS_PASSWORD=redispass

# App
PORT=8080
APP_PREFIX=/api/v1
FRONTEND_URL=http://localhost:3000
JWT_SECRET=your_jwt_secret
JWT_EXPIRATION=3600000

# S3 (tuỳ chọn)
S3_BUCKET_NAME=
S3_ACCESS_KEY_ID=
S3_SECRET_ACCESS_KEY=
S3_REGION=

# Mail (tuỳ chọn)
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=
MAIL_PASSWORD=

```

> Lưu ý: `docker-compose.yml` dùng biến `${REDIS_PASSWORD}` để khởi tạo Redis container.

-----

## Chạy ứng dụng
### 1) Chạy bằng Docker Compose (Postgres + Redis)
Trước hết tạo file `.env` theo mẫu ở trên rồi chạy:

```
# Windows (cmd.exe)
docker compose up -d
```

Kiểm tra logs / status:
```
docker compose ps
docker compose logs -f
```

### 2) Chạy ứng dụng Spring Boot local (profile `dev` dùng cấu hình Redis ở `application-dev.yml`)
Khởi chạy Redis (nếu không dùng docker-compose) hoặc dùng container Redis:
```
# đơn giản: chạy Redis bằng docker
docker run -d --name redis -p 6379:6379 redis:7-alpine redis-server --requirepass redispass
```

Chạy ứng dụng:
```
# Windows
mvnw.cmd clean package -DskipTests
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

# hoặc chạy jar
mvnw.cmd clean package -DskipTests
java -jar target/gym-health-tech-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### 3) Test & kiểm tra
- Build + test:
```
mvnw.cmd test
```
- OpenAPI UI (SpringDoc): mặc định có, truy cập `http://localhost:8080/swagger-ui.html` hoặc `http://localhost:8080/swagger-ui/index.html`
-----

## Tests & Lint
- Có sẵn `mvnw.cmd test` để chạy unit/integration tests
- Spotless + Checkstyle config đã có trong `pom.xml` và thư mục `checkstyle/`
