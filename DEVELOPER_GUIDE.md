# 🧑‍💻 DEVELOPER GUIDE – Gym App Backend (Clean Architecture + SOLID)

## 1. Kiến trúc tổng thể

Dự án tuân theo Clean Architecture kết hợp Domain-Driven Design (DDD) nhẹ.
### 🔧 Cấu trúc thư mục:
```
src/main/java/com/example/gymapp/
├── domain/            # Model & Interface (Entity, VO, Port)
├── application/       # Use case (Service, DTO)
├── adapter/web/       # REST Controller, Request/Response
├── infrastructure/    # Repository, AI, Config, Security
```
### 🔁 Luồng phụ thuộc:
- `adapter` → `application` → `domain`
- `infrastructure` KHÔNG gọi ngược vào domain hoặc controller

## 2. Đặt tên & tổ chức code

| Loại        | Quy ước                     |
|-------------|-----------------------------|
| Biến        | camelCase (`userId`)        |
| Hàm         | động từ + danh từ (`getUserInfo()`) |
| Class       | PascalCase (`UserService`)  |
| Interface   | `UserRepository`, `AIService` |
| DTO         | `RegisterUserRequest`, `WorkoutPlanResponse` |
| Constants   | `static final`: UPPER_CASE  |
| Enum        | UPPER_SNAKE_CASE            |

## 3. Format code – Spotless (Google Java Format)

### ✅ Cài plugin Spotless (Gradle)
```groovy
plugins {
  id 'com.diffplug.spotless' version '6.20.0'
}
spotless {
  java {
    googleJavaFormat()
    removeUnusedImports()
    trimTrailingWhitespace()
    endWithNewline()
  }
}
```

### 👉 Format bằng lệnh:
```bash
./gradlew spotlessApply
```

## 4. Quy tắc viết code & comment

- Chỉ comment những logic khó hiểu, không lặp lại điều hiển nhiên
- Mỗi class cần có trách nhiệm rõ ràng (Single Responsibility)
- Tách logic ra khỏi controller – không viết trực tiếp trong controller

## 5. Git hook (pre-commit)

Tạo file `.git/hooks/pre-commit`:
```bash
#!/bin/sh
./gradlew spotlessCheck || exit 1
./gradlew test || exit 1
```
```bash
chmod +x .git/hooks/pre-commit
```

## 6. Test code

- Viết test cho từng Use Case (`application.service`) → mock `port`
- Dùng `JUnit5 + Mockito`
- (Optional) Kiểm tra coverage bằng `JaCoCo`

## 7. CI/CD

- Push code vào `main` sẽ trigger GitHub Actions:
  - Build + test
  - Deploy lên EC2 (Docker Compose)
- Secrets trong GitHub:
  - `HOST`, `PRIVATE_KEY`, `DEPLOY_PATH`

## 8. Logging & Debug

- Dùng `@Slf4j` để log (log ra file/console)
- Chia mức độ log: `info`, `warn`, `error`
- Không log thông tin nhạy cảm (password, token)

## 9. Đảm bảo bảo mật

- Bảo vệ tất cả API bằng JWT (trừ `/auth/**`)
- Swagger chỉ bật ở môi trường `dev`
- Đổi tất cả `secret` thành biến môi trường

## 10. Tài liệu bắt buộc có trong repo

| Tên file               | Mục đích |
|------------------------|----------|
| `README.md`            | Hướng dẫn cài & chạy |
| `DEVELOPER_GUIDE.md`   | Tài liệu code chuẩn |
| `ARCHITECTURE_RULES.md`| Mô tả Clean Architecture |
| `API_DOC.md` (Swagger) | Mô tả API |