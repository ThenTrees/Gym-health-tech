-- Extensions cần cho schema/migration
CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Tạo app user (nếu chưa có). KHÔNG dùng superuser cho ứng dụng.
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'gymhealthtech') THEN
CREATE ROLE gymhealthtech LOGIN PASSWORD 'gymhealthtech';
END IF;
END$$;

-- Cấp quyền cơ bản cho gymhealthtech trên DB và schema public
GRANT CONNECT ON DATABASE gymhealthtech TO gymhealthtech;
GRANT USAGE ON SCHEMA public TO gymhealthtech;

-- Quyền mặc định cho các bảng/sequence tạo SAU NÀY (Flyway sẽ tạo)
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO gymhealthtech;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO gymhealthtech;

-- (tuỳ chọn) đặt search_path cho gymhealthtech
ALTER ROLE gymhealthtech IN DATABASE gymhealthtech SET search_path TO public;
