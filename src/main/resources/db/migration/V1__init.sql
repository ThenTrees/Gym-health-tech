-- V1__final.sql — Gym AI Coaching (PostgreSQL 14+)
-- One-shot init for final schema (all modules + hardening + seeds)

-----------------------------
-- 0) EXTENSIONS & HELPERS --
-----------------------------
CREATE EXTENSION IF NOT EXISTS pgcrypto;  -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS citext;    -- case-insensitive strings (email)

-- updated_at trigger
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger AS $$
BEGIN
  NEW.updated_at := now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- optimistic locking (version)
CREATE OR REPLACE FUNCTION bump_version() RETURNS trigger AS $$
BEGIN
  NEW.version := COALESCE(OLD.version, 0) + 1;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

----------------------
-- 1) ENUMERATIONS  --
----------------------
DO $$ BEGIN CREATE TYPE user_status AS ENUM (
  'ACTIVE',
  'INACTIVE',
  'PENDING_VERIFICATION',
  'DELETED',
  'SUSPENDED'
  );                 EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE user_role        AS ENUM ('USER','ADMIN','GUEST');                       EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE gender_type      AS ENUM ('MALE','FEMALE','OTHER');                EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE objective_type   AS ENUM ('LOSE_FAT','GAIN_MUSCLE','ENDURANCE','MAINTAIN'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE plan_status_type AS ENUM ('DRAP','ACTIVE','COMPLETED','ARCHIVED'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE plan_source_type AS ENUM ('AI','TEMPLATE','CUSTOM');               EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE session_status   AS ENUM ('IN_PROGRESS','COMPLETED','CANCELLED','ABANDONED'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE asset_type       AS ENUM ('VIDEO','IMAGE');                        EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE exercise_level   AS ENUM ('BEGINNER','INTERMEDIATE','ADVANCE');   EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE notification_kind   AS ENUM ('SESSION_REMINDER','REST_TIMER','SYSTEM'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE notification_status AS ENUM ('SCHEDULED','SENT','CANCELLED','FAILED');    EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE subscription_tier    AS ENUM ('FREE','PRO');                       EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE subscription_platform AS ENUM ('WEB','STRIPE','IOS','ANDROID');    EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE subscription_status AS ENUM ('ACTIVE','TRIAL','EXPIRED','CANCELLED');     EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE verification_type AS ENUM ('EMAIL_VERIFY','PASSWORD_RESET','LOGIN_OTP');  EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE device_platform  AS ENUM ('IOS','ANDROID','WEB');                  EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-----------------------------
-- 2) IDENTITY & SECURITY  --
-----------------------------
CREATE TABLE IF NOT EXISTS users (
                                   id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                   email          citext NOT NULL UNIQUE,
                                   phone          varchar(20) UNIQUE,
                                   password_hash  text NOT NULL,
                                   status         user_status NOT NULL DEFAULT 'ACTIVE',
                                   role           user_role   NOT NULL DEFAULT 'USER',
                                   email_verified boolean NOT NULL DEFAULT false,
                                   created_at     timestamptz NOT NULL DEFAULT now(),
                                   updated_at     timestamptz NOT NULL DEFAULT now(),
                                   version        int NOT NULL DEFAULT 0
);
DO $$ BEGIN
  CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_users_version BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION bump_version();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

CREATE TABLE IF NOT EXISTS user_profiles (
                                           user_id      uuid PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                                           full_name    varchar(120),
                                           gender       gender_type,
                                           dob          date,
                                           height_cm    numeric(5,2) CHECK (height_cm IS NULL OR height_cm BETWEEN 50 AND 250),
                                           weight_kg    numeric(5,2) CHECK (weight_kg IS NULL OR weight_kg BETWEEN 20 AND 400),
                                           bmi          numeric(5,2),
                                           health_notes text,
                                           timezone     varchar(64) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh',
                                           unit_weight  varchar(8) NOT NULL DEFAULT 'kg' CHECK (unit_weight IN ('kg','lb')),
                                           unit_length  varchar(8) NOT NULL DEFAULT 'cm' CHECK (unit_length IN ('cm','in')),
                                           created_at   timestamptz NOT NULL DEFAULT now(),
                                           updated_at   timestamptz NOT NULL DEFAULT now(),
                                           version      int NOT NULL DEFAULT 0
);
DO $$ BEGIN
  CREATE TRIGGER trg_user_profiles_updated_at BEFORE UPDATE ON user_profiles FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_user_profiles_version BEFORE UPDATE ON user_profiles FOR EACH ROW EXECUTE FUNCTION bump_version();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

CREATE TABLE IF NOT EXISTS user_measurements (
                                               id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                               user_id     uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                               measured_at date NOT NULL,
                                               weight_kg   numeric(5,2),
                                               bodyfat_pct numeric(4,1) CHECK (bodyfat_pct IS NULL OR (bodyfat_pct BETWEEN 2 AND 70)),
                                               waist_cm    numeric(5,2),
                                               hip_cm      numeric(5,2),
                                               chest_cm    numeric(5,2),
                                               notes       text,
                                               UNIQUE(user_id, measured_at)
);
CREATE INDEX IF NOT EXISTS idx_measurements_user_time ON user_measurements(user_id, measured_at DESC);

CREATE TABLE IF NOT EXISTS refresh_tokens (
                                            id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                            user_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                            token_hash   text NOT NULL,
                                            issued_at    timestamptz NOT NULL DEFAULT now(),
                                            expires_at   timestamptz NOT NULL,
                                            revoked_at   timestamptz,
                                            user_agent   text,
                                            ip           inet,
                                            UNIQUE (user_id, token_hash)
);
CREATE INDEX IF NOT EXISTS idx_refresh_user_active ON refresh_tokens(user_id) WHERE revoked_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

CREATE TABLE IF NOT EXISTS verification_tokens (
                                                 id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                                 user_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                                 type         verification_type NOT NULL,
                                                 token_hash   text NOT NULL,
                                                 expires_at   timestamptz NOT NULL,
                                                 consumed_at  timestamptz,
                                                 created_at   timestamptz NOT NULL DEFAULT now(),
                                                 UNIQUE (type, token_hash)
);
CREATE INDEX IF NOT EXISTS idx_vt_user_type ON verification_tokens(user_id, type);
CREATE INDEX IF NOT EXISTS idx_verification_tokens_expires_at ON verification_tokens(expires_at);

CREATE TABLE IF NOT EXISTS oauth_accounts (
                                            id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                            user_id           uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                            provider          varchar(32) NOT NULL,
                                            provider_user_id  varchar(128) NOT NULL,
                                            email             citext,
                                            created_at        timestamptz NOT NULL DEFAULT now(),
                                            UNIQUE (provider, provider_user_id)
);
CREATE INDEX IF NOT EXISTS idx_oauth_user ON oauth_accounts(user_id);

CREATE TABLE IF NOT EXISTS login_attempts (
                                            id           bigserial PRIMARY KEY,
                                            user_id      uuid REFERENCES users(id) ON DELETE SET NULL,
                                            email        citext,
                                            success      boolean NOT NULL,
                                            ip           inet,
                                            user_agent   text,
                                            occurred_at  timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_login_email_time ON login_attempts(email, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_login_user_time  ON login_attempts(user_id, occurred_at DESC);

--------------
-- 3) GOALS --
--------------
CREATE TABLE IF NOT EXISTS goals (
                                   id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                   user_id            uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                   objective          objective_type NOT NULL,
                                   sessions_per_week  int NOT NULL CHECK (sessions_per_week BETWEEN 1 AND 14),
                                   session_minutes    int NOT NULL CHECK (session_minutes BETWEEN 10 AND 180),
                                   preferences        jsonb,
                                   started_at         date NOT NULL DEFAULT current_date,
                                   ended_at           date,
                                   created_at         timestamptz NOT NULL DEFAULT now(),
                                   CONSTRAINT goals_end_ge_start CHECK (ended_at IS NULL OR ended_at >= started_at)
);
CREATE INDEX IF NOT EXISTS idx_goals_user ON goals(user_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_goal_active_per_user ON goals(user_id) WHERE ended_at IS NULL;

--------------------------------------
-- 4) EXERCISE LIBRARY & TAXONOMIES --
--------------------------------------
CREATE TABLE IF NOT EXISTS muscles (
                                     code varchar(32) PRIMARY KEY,
                                     name varchar(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS equipment_types (
                                             code varchar(32) PRIMARY KEY,
                                             name varchar(64) NOT NULL
);

-- seed minimal taxonomy
INSERT INTO muscles(code,name) VALUES
                                 ('quads','Quadriceps'),('hamstrings','Hamstrings'),('glutes','Glutes'),('chest','Chest'),
                                 ('back','Back'),('shoulders','Shoulders'),('biceps','Biceps'),('triceps','Triceps'),('core','Core')
ON CONFLICT (code) DO NOTHING;

INSERT INTO equipment_types(code,name) VALUES
                                         ('bodyweight','Bodyweight'),('dumbbell','Dumbbell'),('barbell','Barbell'),
                                         ('band','Resistance Band'),('machine','Machine'),('kettlebell','Kettlebell')
ON CONFLICT (code) DO NOTHING;

CREATE TABLE IF NOT EXISTS exercises (
                                       id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                       slug             varchar(80) NOT NULL UNIQUE,
                                       name             varchar(120) NOT NULL,
                                       level            exercise_level NOT NULL,
                                       primary_muscle   varchar(32) REFERENCES muscles(code) ON DELETE RESTRICT,
                                       equipment        varchar(32) REFERENCES equipment_types(code) ON DELETE RESTRICT,
                                       instructions     text,
                                       safety_notes     text,
                                       thumbnail_url    text,
                                       created_at       timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_exercises_primary_muscle ON exercises(primary_muscle);
CREATE INDEX IF NOT EXISTS idx_exercises_equipment      ON exercises(equipment);

CREATE TABLE IF NOT EXISTS exercise_muscles (
                                              exercise_id uuid NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
                                              muscle_code varchar(32) NOT NULL REFERENCES muscles(code) ON DELETE RESTRICT,
                                              role        varchar(16) NOT NULL CHECK (role IN ('PRIMARY','SECONDARY')),
                                              PRIMARY KEY (exercise_id, muscle_code, role)
);

CREATE TABLE IF NOT EXISTS content_assets (
                                            id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                            asset_type   asset_type NOT NULL,
                                            url          text NOT NULL,
                                            mime_type    varchar(64),
                                            sha256_hex   char(64),
                                            duration_s   int,
                                            width        int,
                                            height       int,
                                            size_bytes   bigint,
                                            created_at   timestamptz NOT NULL DEFAULT now(),
                                            UNIQUE (url)
);
-- dedupe theo hash khi có
DO $$ BEGIN
  CREATE UNIQUE INDEX uq_assets_sha256_notnull
    ON content_assets(sha256_hex)
    WHERE sha256_hex IS NOT NULL;
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

CREATE TABLE IF NOT EXISTS exercise_assets (
                                             exercise_id uuid NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
                                             asset_id    uuid NOT NULL REFERENCES content_assets(id) ON DELETE CASCADE,
                                             sort_order  int NOT NULL DEFAULT 1,
                                             PRIMARY KEY (exercise_id, asset_id)
);

------------------------------
-- 5) PLANS (PRESCRIPTIONS) --
------------------------------
CREATE TABLE IF NOT EXISTS plans (
                                   id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                   user_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                   goal_id      uuid REFERENCES goals(id) ON DELETE SET NULL,
                                   title        varchar(120),
                                   source       plan_source_type NOT NULL DEFAULT 'AI',
                                   cycle_weeks  int NOT NULL DEFAULT 4 CHECK (cycle_weeks BETWEEN 1 AND 16),
                                   status       plan_status_type NOT NULL DEFAULT 'ACTIVE',
                                   created_at   timestamptz NOT NULL DEFAULT now(),
                                   updated_at   timestamptz NOT NULL DEFAULT now(),
                                   version      int NOT NULL DEFAULT 0
);
DO $$ BEGIN
  CREATE TRIGGER trg_plans_updated_at BEFORE UPDATE ON plans FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TRIGGER trg_plans_version BEFORE UPDATE ON plans FOR EACH ROW EXECUTE FUNCTION bump_version();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
CREATE INDEX IF NOT EXISTS idx_plans_user_status ON plans(user_id, status);

CREATE TABLE IF NOT EXISTS plan_days (
                                       id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                       plan_id        uuid NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
                                       day_index      int NOT NULL CHECK (day_index >= 1),
                                       split_name     varchar(50),
                                       scheduled_date date,
                                       created_at     timestamptz NOT NULL DEFAULT now(),
                                       UNIQUE (plan_id, day_index)
);
CREATE INDEX IF NOT EXISTS idx_plan_days_plan_date ON plan_days(plan_id, scheduled_date);

CREATE TABLE IF NOT EXISTS plan_items (
                                        id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                        plan_day_id  uuid NOT NULL REFERENCES plan_days(id) ON DELETE CASCADE,
                                        exercise_id  uuid NOT NULL REFERENCES exercises(id) ON DELETE RESTRICT,
                                        item_index   int  NOT NULL CHECK (item_index >= 1),
                                        prescription jsonb NOT NULL,
                                        notes        text,
                                        created_at   timestamptz NOT NULL DEFAULT now(),
                                        UNIQUE (plan_day_id, item_index),
                                        CONSTRAINT prescription_is_object CHECK (jsonb_typeof(prescription) = 'object'),
                                        CONSTRAINT prescription_sets_chk CHECK ((prescription ? 'sets') AND ((prescription->>'sets')::int >= 1))
);
DO $$ BEGIN
  CREATE INDEX idx_plan_items_prescription_gin ON plan_items USING GIN (prescription);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

--------------------------------
-- 6) SESSIONS (EXECUTIONS)   --
--------------------------------
CREATE TABLE IF NOT EXISTS sessions (
                                      id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                      user_id       uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                      plan_day_id   uuid REFERENCES plan_days(id) ON DELETE SET NULL,
                                      started_at    timestamptz NOT NULL DEFAULT now(),
                                      ended_at      timestamptz,
                                      status        session_status NOT NULL DEFAULT 'IN_PROGRESS',
                                      session_rpe   numeric(3,1) CHECK (session_rpe IS NULL OR (session_rpe BETWEEN 0 AND 10)),
                                      notes         text,
                                      created_at    timestamptz NOT NULL DEFAULT now(),
                                      CONSTRAINT sessions_end_ge_start CHECK (ended_at IS NULL OR ended_at >= started_at)
);
CREATE INDEX IF NOT EXISTS idx_sessions_user_time ON sessions(user_id, started_at DESC);
CREATE INDEX IF NOT EXISTS idx_sessions_plan_day  ON sessions(plan_day_id);
CREATE INDEX IF NOT EXISTS idx_sessions_user_status_time ON sessions(user_id, status, started_at DESC);

CREATE TABLE IF NOT EXISTS session_sets (
                                          id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                          session_id   uuid NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
                                          exercise_id  uuid NOT NULL REFERENCES exercises(id) ON DELETE RESTRICT,
                                          set_index    int NOT NULL CHECK (set_index >= 1),
                                          planned      jsonb,
                                          actual       jsonb,
                                          created_at   timestamptz NOT NULL DEFAULT now(),
                                          UNIQUE (session_id, exercise_id, set_index),
                                          CONSTRAINT actual_obj_chk CHECK (actual IS NULL OR jsonb_typeof(actual)='object'),
                                          CONSTRAINT actual_rpe_chk CHECK (actual IS NULL OR ((actual ? 'rpe') = false OR ((actual->>'rpe')::numeric BETWEEN 0 AND 10)))
);
DO $$ BEGIN
  CREATE INDEX idx_session_sets_session ON session_sets(session_id);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE INDEX idx_session_sets_actual_gin ON session_sets USING GIN (actual);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

--------------------------------------
-- 7) NOTIFICATIONS & DEVICE TOKENS --
--------------------------------------
CREATE TABLE IF NOT EXISTS device_tokens (
                                           id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                           user_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                           platform     device_platform NOT NULL,   -- ios/android/web
                                           push_token   text NOT NULL,
                                           enabled      boolean NOT NULL DEFAULT true,
                                           created_at   timestamptz NOT NULL DEFAULT now(),
                                           UNIQUE (user_id, platform, push_token)
);

CREATE TABLE IF NOT EXISTS notifications (
                                           id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                           user_id       uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                           kind          notification_kind NOT NULL,
                                           scheduled_at  timestamptz NOT NULL,
                                           status        notification_status NOT NULL DEFAULT 'SCHEDULED',
                                           payload       jsonb,
                                           created_at    timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_notifications_user_time        ON notifications(user_id, scheduled_at);
CREATE INDEX IF NOT EXISTS idx_notifications_status           ON notifications(status);
CREATE INDEX IF NOT EXISTS idx_notifications_user_status_time ON notifications(user_id, status, scheduled_at);

-------------------
-- 8) CHATBOT     --
-------------------
CREATE TABLE IF NOT EXISTS chatbot_threads (
                                             id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                             user_id     uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                             created_at  timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_chat_threads_user ON chatbot_threads(user_id, created_at DESC);

CREATE TABLE IF NOT EXISTS chatbot_messages (
                                              id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                              thread_id   uuid NOT NULL REFERENCES chatbot_threads(id) ON DELETE CASCADE,
                                              user_id     uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                              role        varchar(16) NOT NULL CHECK (role IN ('USER','ASSISTANT','SYSTEM')),
                                              content     text NOT NULL,
                                              created_at  timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_chatbot_user_time ON chatbot_messages(user_id, created_at);

-----------------------------
-- 9) REPORTS / AGGREGATES --
-----------------------------
CREATE TABLE IF NOT EXISTS reports_cache (
                                           id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                           user_id       uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                           scope         varchar(16) NOT NULL CHECK (scope IN ('week','month')),
                                           period_start  date NOT NULL,
                                           payload       jsonb NOT NULL,
                                           generated_at  timestamptz NOT NULL DEFAULT now(),
                                           UNIQUE (user_id, scope, period_start)
);

--------------------------------
-- 10) MONETIZATION & ACCESS  --
--------------------------------
CREATE TABLE IF NOT EXISTS subscriptions (
                                           id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                           user_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                           tier         subscription_tier NOT NULL DEFAULT 'FREE',
                                           platform     subscription_platform NOT NULL,
                                           status       subscription_status NOT NULL DEFAULT 'ACTIVE',
                                           started_at   timestamptz NOT NULL DEFAULT now(),
                                           expires_at   timestamptz,
                                           cancelled_at timestamptz,
                                           created_at   timestamptz NOT NULL DEFAULT now(),
                                           updated_at   timestamptz NOT NULL DEFAULT now()
);
DO $$ BEGIN
  CREATE TRIGGER trg_subscriptions_updated_at BEFORE UPDATE ON subscriptions FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- at most 1 active/trial per user
CREATE UNIQUE INDEX IF NOT EXISTS uq_subscription_active_per_user
  ON subscriptions(user_id) WHERE status IN ('ACTIVE','TRIAL');

DO $$ BEGIN
  CREATE TABLE IF NOT EXISTS payments (
                                        id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                        user_id          uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                        provider         varchar(16) NOT NULL CHECK (provider IN ('stripe','apple','google')),
                                        provider_txn_id  varchar(128) NOT NULL,
                                        amount_cents     int NOT NULL CHECK (amount_cents >= 0),
                                        currency         varchar(8) NOT NULL DEFAULT 'USD',
                                        status           varchar(16) NOT NULL CHECK (status IN ('PENDING','SUCCEEDED','FAILED','REFUNDED')),
                                        meta             jsonb,
                                        created_at       timestamptz NOT NULL DEFAULT now(),
                                        updated_at       timestamptz NOT NULL DEFAULT now(),
                                        UNIQUE (provider, provider_txn_id)
  );
  CREATE TRIGGER trg_payments_updated_at BEFORE UPDATE ON payments FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
CREATE INDEX IF NOT EXISTS idx_payments_user_time ON payments(user_id, created_at DESC);

CREATE TABLE IF NOT EXISTS features (
                                      key        varchar(64) PRIMARY KEY,
                                      name       varchar(120) NOT NULL,
                                      pro_only   boolean NOT NULL DEFAULT true
);
INSERT INTO features(key,name,pro_only) VALUES
                                          ('export_pdf','Export reports as PDF', true),
                                          ('advanced_reports','Advanced progress analytics', true),
                                          ('nutrition','Meal planning & targets', true),
                                          ('offline_video','Offline video cache', true)
ON CONFLICT (key) DO NOTHING;


CREATE TABLE IF NOT EXISTS entitlements (
                                          id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                          user_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                          feature_key  varchar(64) NOT NULL REFERENCES features(key) ON DELETE RESTRICT,
                                          limits       jsonb,
                                          source       varchar(16) NOT NULL DEFAULT 'subscription' CHECK (source IN ('subscription','promo','gift','admin')),
                                          starts_at    timestamptz NOT NULL DEFAULT now(),
                                          expires_at   timestamptz,
                                          created_at   timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_entitlements_user_feature ON entitlements(user_id, feature_key);
CREATE INDEX IF NOT EXISTS idx_entitlements_expires_at   ON entitlements(user_id, expires_at);

CREATE OR REPLACE FUNCTION enforce_single_active_entitlement()
  RETURNS trigger AS $$
BEGIN
  IF (NEW.expires_at IS NULL OR NEW.expires_at > now()) AND
     EXISTS (SELECT 1 FROM entitlements e
             WHERE e.user_id = NEW.user_id
               AND e.feature_key = NEW.feature_key
               AND (e.expires_at IS NULL OR e.expires_at > now())
               AND e.id <> NEW.id) THEN
    RAISE EXCEPTION 'User % already has an active entitlement for feature %',
      NEW.user_id, NEW.feature_key;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$ BEGIN
  CREATE TRIGGER trg_entitlements_single_active
    BEFORE INSERT OR UPDATE ON entitlements
    FOR EACH ROW EXECUTE FUNCTION enforce_single_active_entitlement();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;


--------------------------
-- 11) NUTRITION (PRO)  --
--------------------------
CREATE TABLE IF NOT EXISTS nutrition_targets (
                                               id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                               user_id       uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                               goal_id       uuid REFERENCES goals(id) ON DELETE SET NULL,
                                               calories_kcal int NOT NULL CHECK (calories_kcal BETWEEN 800 AND 5000),
                                               protein_g     int NOT NULL CHECK (protein_g BETWEEN 30 AND 400),
                                               fat_g         int NOT NULL CHECK (fat_g BETWEEN 20 AND 200),
                                               carbs_g       int NOT NULL CHECK (carbs_g BETWEEN 0 AND 800),
                                               created_at    timestamptz NOT NULL DEFAULT now(),
                                               UNIQUE (user_id, goal_id)
);

CREATE TABLE IF NOT EXISTS meal_plans (
                                        id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                        user_id     uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                        plan_date   date NOT NULL,
                                        meta        jsonb,
                                        created_at  timestamptz NOT NULL DEFAULT now(),
                                        UNIQUE (user_id, plan_date)
);
CREATE INDEX IF NOT EXISTS idx_meal_plans_user_date ON meal_plans(user_id, plan_date);

-------------------------
-- 12) GAMIFICATION     --
-------------------------
CREATE TABLE IF NOT EXISTS challenges (
                                        id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                        title       varchar(120) NOT NULL,
                                        start_date  date NOT NULL,
                                        end_date    date NOT NULL,
                                        rules       jsonb,
                                        created_at  timestamptz NOT NULL DEFAULT now(),
                                        CONSTRAINT challenges_end_ge_start CHECK (end_date >= start_date)
);

CREATE TABLE IF NOT EXISTS challenge_participants (
                                                    challenge_id uuid NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
                                                    user_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                                    joined_at    timestamptz NOT NULL DEFAULT now(),
                                                    progress     jsonb,
                                                    PRIMARY KEY (challenge_id, user_id)
);

CREATE TABLE IF NOT EXISTS badges (
                                    key        varchar(64) PRIMARY KEY,
                                    name       varchar(120) NOT NULL,
                                    icon_url   text
);

CREATE TABLE IF NOT EXISTS user_badges (
                                         user_id    uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                         badge_key  varchar(64) NOT NULL REFERENCES badges(key) ON DELETE RESTRICT,
                                         awarded_at timestamptz NOT NULL DEFAULT now(),
                                         PRIMARY KEY (user_id, badge_key)
);

------------------
-- 13) COMMENTS  --
------------------
COMMENT ON TABLE users            IS 'User accounts (email CITEXT), role/status enums, email_verified, optimistic locking.';
COMMENT ON TABLE user_profiles    IS '1-1 profile with units & timezone; anthropometrics.';
COMMENT ON TABLE user_measurements IS 'Time-series measurements for progress.';
COMMENT ON TABLE goals            IS 'Training goals; single active enforced by partial unique.';
COMMENT ON TABLE muscles          IS 'Muscle taxonomy.';
COMMENT ON TABLE equipment_types  IS 'Equipment taxonomy.';
COMMENT ON TABLE exercises        IS 'Exercise dictionary, normalized to muscles/equipment.';
COMMENT ON TABLE exercise_muscles IS 'n:n mapping (primary/secondary).';
COMMENT ON TABLE content_assets   IS 'CDN assets; sha256 for dedupe.';
COMMENT ON TABLE exercise_assets  IS 'Junction: exercises <-> assets.';
COMMENT ON TABLE plans            IS 'Training plan (AI/template/custom).';
COMMENT ON TABLE plan_days        IS 'Days inside a plan; unique order.';
COMMENT ON TABLE plan_items       IS 'Prescription per exercise (JSONB with checks).';
COMMENT ON TABLE sessions         IS 'Workout sessions; keep history if plan removed.';
COMMENT ON TABLE session_sets     IS 'Per-set logs (planned vs actual).';
COMMENT ON TABLE device_tokens    IS 'Push tokens per device/platform.';
COMMENT ON TABLE notifications    IS 'Scheduled reminders and system messages.';
COMMENT ON TABLE chatbot_threads  IS 'Chat threads.';
COMMENT ON TABLE chatbot_messages IS 'Chat messages linked to threads.';
COMMENT ON TABLE reports_cache    IS 'Aggregates for dashboards/exports.';
COMMENT ON TABLE subscriptions    IS 'Subscription states; 1 active/trial per user.';
COMMENT ON TABLE payments         IS 'Payment transactions (Stripe/Apple/Google).';
COMMENT ON TABLE features         IS 'Feature catalog used by entitlements.';
COMMENT ON TABLE entitlements     IS 'Feature access grants per user (partial unique active).';
COMMENT ON TABLE nutrition_targets IS 'Macro/calorie targets.';
COMMENT ON TABLE meal_plans       IS 'Day-level meal plans as JSON.';
COMMENT ON TABLE challenges       IS 'Challenge definitions.';
COMMENT ON TABLE challenge_participants IS 'User participation & progress.';
COMMENT ON TABLE badges           IS 'Badges catalog.';
COMMENT ON TABLE user_badges      IS 'Awards per user.';
