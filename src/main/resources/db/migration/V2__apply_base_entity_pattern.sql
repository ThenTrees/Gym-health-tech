-- V2__apply_base_entity_pattern.sql
-- Add soft delete and audit fields to existing tables

-----------------------------
-- 1) ADD SOFT DELETE HELPER FUNCTIONS
-----------------------------

-- Soft delete function for all tables
CREATE OR REPLACE FUNCTION soft_delete() RETURNS trigger AS $$
BEGIN
  NEW.is_deleted := true;
  NEW.deleted_at := now();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Custom where clause for soft delete (for JPA @Where)
-- Already handled by application layer, but good to have for direct SQL queries

-----------------------------
-- 2) UPDATE MAIN ENTITY TABLES
-----------------------------

-- Users table (most important)
ALTER TABLE users
  ADD COLUMN IF NOT EXISTS is_deleted boolean NOT NULL DEFAULT false,
  ADD COLUMN IF NOT EXISTS deleted_at timestamptz,
  ADD COLUMN IF NOT EXISTS created_by uuid,
  ADD COLUMN IF NOT EXISTS updated_by uuid;

-- User profiles
ALTER TABLE user_profiles
  ADD COLUMN IF NOT EXISTS is_deleted boolean NOT NULL DEFAULT false,
  ADD COLUMN IF NOT EXISTS deleted_at timestamptz,
  ADD COLUMN IF NOT EXISTS created_by uuid,
  ADD COLUMN IF NOT EXISTS updated_by uuid;

-- Goals
ALTER TABLE goals
  ADD COLUMN IF NOT EXISTS is_deleted boolean NOT NULL DEFAULT false,
  ADD COLUMN IF NOT EXISTS deleted_at timestamptz,
  ADD COLUMN IF NOT EXISTS created_by uuid,
  ADD COLUMN IF NOT EXISTS updated_by uuid,
  ADD COLUMN IF NOT EXISTS updated_at timestamptz NOT NULL DEFAULT now(),
  ADD COLUMN IF NOT EXISTS version int NOT NULL DEFAULT 0;

-- Add triggers for goals
DO $$ BEGIN
CREATE TRIGGER trg_goals_updated_at BEFORE UPDATE ON goals FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
CREATE TRIGGER trg_goals_version BEFORE UPDATE ON goals FOR EACH ROW EXECUTE FUNCTION bump_version();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- Exercises
ALTER TABLE exercises
  ADD COLUMN IF NOT EXISTS is_deleted boolean NOT NULL DEFAULT false,
  ADD COLUMN IF NOT EXISTS deleted_at timestamptz,
  ADD COLUMN IF NOT EXISTS created_by uuid,
  ADD COLUMN IF NOT EXISTS updated_by uuid,
  ADD COLUMN IF NOT EXISTS updated_at timestamptz NOT NULL DEFAULT now(),
  ADD COLUMN IF NOT EXISTS version int NOT NULL DEFAULT 0;

-- Add triggers for exercises
DO $$ BEGIN
CREATE TRIGGER trg_exercises_updated_at BEFORE UPDATE ON exercises FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
CREATE TRIGGER trg_exercises_version BEFORE UPDATE ON exercises FOR EACH ROW EXECUTE FUNCTION bump_version();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- Sessions
ALTER TABLE sessions
  ADD COLUMN IF NOT EXISTS is_deleted boolean NOT NULL DEFAULT false,
  ADD COLUMN IF NOT EXISTS deleted_at timestamptz,
  ADD COLUMN IF NOT EXISTS updated_at timestamptz NOT NULL DEFAULT now(),
  ADD COLUMN IF NOT EXISTS version int NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS created_by uuid,
  ADD COLUMN IF NOT EXISTS updated_by uuid;

-- Add triggers for sessions
DO $$ BEGIN
CREATE TRIGGER trg_sessions_updated_at BEFORE UPDATE ON sessions FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
CREATE TRIGGER trg_sessions_version BEFORE UPDATE ON sessions FOR EACH ROW EXECUTE FUNCTION bump_version();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- Session sets
ALTER TABLE session_sets
  ADD COLUMN IF NOT EXISTS is_deleted boolean NOT NULL DEFAULT false,
  ADD COLUMN IF NOT EXISTS deleted_at timestamptz,
  ADD COLUMN IF NOT EXISTS updated_at timestamptz NOT NULL DEFAULT now(),
  ADD COLUMN IF NOT EXISTS version int NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS created_by uuid,
  ADD COLUMN IF NOT EXISTS updated_by uuid;

-- Add triggers for session_sets
DO $$ BEGIN
CREATE TRIGGER trg_session_sets_updated_at BEFORE UPDATE ON session_sets FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
CREATE TRIGGER trg_session_sets_version BEFORE UPDATE ON session_sets FOR EACH ROW EXECUTE FUNCTION bump_version();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- Content assets
ALTER TABLE content_assets
  ADD COLUMN IF NOT EXISTS is_deleted boolean NOT NULL DEFAULT false,
  ADD COLUMN IF NOT EXISTS deleted_at timestamptz,
  ADD COLUMN IF NOT EXISTS updated_at timestamptz NOT NULL DEFAULT now(),
  ADD COLUMN IF NOT EXISTS version int NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS created_by uuid,
  ADD COLUMN IF NOT EXISTS updated_by uuid;

-- Add triggers for content_assets
DO $$ BEGIN
CREATE TRIGGER trg_content_assets_updated_at BEFORE UPDATE ON content_assets FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
CREATE TRIGGER trg_content_assets_version BEFORE UPDATE ON content_assets FOR EACH ROW EXECUTE FUNCTION bump_version();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- Subscriptions (already has some fields)
ALTER TABLE subscriptions
  ADD COLUMN IF NOT EXISTS is_deleted boolean NOT NULL DEFAULT false,
  ADD COLUMN IF NOT EXISTS deleted_at timestamptz,
  ADD COLUMN IF NOT EXISTS created_by uuid,
  ADD COLUMN IF NOT EXISTS updated_by uuid,
  ADD COLUMN IF NOT EXISTS version int NOT NULL DEFAULT 0;

-- Add version trigger for subscriptions
DO $$ BEGIN
CREATE TRIGGER trg_subscriptions_version BEFORE UPDATE ON subscriptions FOR EACH ROW EXECUTE FUNCTION bump_version();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-----------------------------
-- 3) ADD INDEXES FOR SOFT DELETE
-----------------------------

-- Primary indexes for soft delete queries
CREATE INDEX IF NOT EXISTS idx_users_not_deleted ON users(id) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_exercises_not_deleted ON exercises(id) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_sessions_not_deleted ON sessions(id) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_goals_not_deleted ON goals(id) WHERE is_deleted = false;

-- Composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_users_email_not_deleted ON users(email) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_exercises_slug_not_deleted ON exercises(slug) WHERE is_deleted = false;

-----------------------------
-- 4) UPDATE FOREIGN KEY CONSTRAINTS (Optional)
-----------------------------

-- You might want to update some FK constraints to handle soft deletes
-- For example, instead of ON DELETE CASCADE, use ON DELETE SET NULL
-- and handle cleanup in application logic

-- Example for sessions -> users (keep sessions when user is soft deleted)
-- ALTER TABLE sessions DROP CONSTRAINT sessions_user_id_fkey;
-- ALTER TABLE sessions ADD CONSTRAINT sessions_user_id_fkey
--   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

-----------------------------
-- 5) CREATE CLEANUP FUNCTIONS
-----------------------------

-- Function to hard delete old soft-deleted records
CREATE OR REPLACE FUNCTION cleanup_soft_deleted(days_old integer DEFAULT 90)
RETURNS void AS $$
BEGIN
  -- Clean up users and cascading data after 90 days of soft delete
DELETE FROM users
WHERE is_deleted = true
  AND deleted_at < now() - interval '1 day' * days_old;

-- Add more cleanup logic for other tables as needed
RAISE NOTICE 'Cleanup completed for records older than % days', days_old;
END;
$$ LANGUAGE plpgsql;

-----------------------------
-- 6) COMMENTS FOR NEW FIELDS
-----------------------------

COMMENT ON COLUMN users.is_deleted IS 'Soft delete flag - false means active';
COMMENT ON COLUMN users.deleted_at IS 'Timestamp when record was soft deleted';
COMMENT ON COLUMN users.created_by IS 'ID of user who created this record';
COMMENT ON COLUMN users.updated_by IS 'ID of user who last updated this record';
