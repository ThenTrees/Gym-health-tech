-- V1__consolidated_gym_health_tech_schema.sql
-- Clean version for PostgreSQL 16

-- Extensions
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- Helper functions
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger AS
$BODY$
BEGIN
  NEW.updated_at := now();
  RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION bump_version() RETURNS trigger AS
$BODY$
BEGIN
  NEW.version := COALESCE(OLD.version, 0) + 1;
  RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

-- Enums
CREATE TYPE goal_status AS ENUM ('ACTIVE','COMPLETED','PAUSED');
CREATE TYPE user_status AS ENUM ('ACTIVE','INACTIVE','PENDING_VERIFICATION','DELETED','SUSPENDED');
CREATE TYPE user_role AS ENUM ('USER','ADMIN','GUEST');
CREATE TYPE gender_type AS ENUM ('MALE','FEMALE','OTHER');
CREATE TYPE objective_type AS ENUM ('LOSE_FAT','GAIN_MUSCLE','ENDURANCE','MAINTAIN');
CREATE TYPE plan_status_type AS ENUM ('DRAFT','ACTIVE','COMPLETED','ARCHIVED');
CREATE TYPE plan_source_type AS ENUM ('AI','TEMPLATE','CUSTOM');
CREATE TYPE session_status AS ENUM ('IN_PROGRESS','COMPLETED','CANCELLED','ABANDONED');
CREATE TYPE exercise_level AS ENUM ('BEGINNER','INTERMEDIATE','ADVANCED');
CREATE TYPE notification_kind AS ENUM ('SESSION_REMINDER','REST_TIMER','SYSTEM');
CREATE TYPE notification_status AS ENUM ('SCHEDULED','SENT','CANCELLED','FAILED');
CREATE TYPE subscription_tier AS ENUM ('FREE','PRO');
CREATE TYPE subscription_platform AS ENUM ('WEB','STRIPE','IOS','ANDROID');
CREATE TYPE subscription_status AS ENUM ('ACTIVE','TRIAL','EXPIRED','CANCELLED');
CREATE TYPE verification_type AS ENUM ('EMAIL_VERIFY','PASSWORD_RESET','LOGIN_OTP');
CREATE TYPE device_platform AS ENUM ('IOS','ANDROID','WEB');
CREATE TYPE muscle_role AS ENUM ('PRIMARY','SECONDARY','STABILIZER');

-- Users table
CREATE TABLE users (
                     id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                     email          citext NOT NULL UNIQUE,
                     phone          varchar(20) UNIQUE,
                     password_hash  text NOT NULL,
                     status         user_status NOT NULL DEFAULT 'ACTIVE',
                     role           user_role   NOT NULL DEFAULT 'USER',
                     email_verified boolean NOT NULL DEFAULT false,
                     created_at     timestamp NOT NULL DEFAULT now(),
                     updated_at     timestamp NOT NULL DEFAULT now(),
                     version        int NOT NULL DEFAULT 0,
                     is_deleted     boolean NOT NULL DEFAULT false,
                     deleted_at     timestamptz,
                     created_by     uuid,
                     updated_by     uuid
);

CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_users_version BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION bump_version();

-- User profiles
CREATE TABLE user_profiles (
                             user_id       uuid PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                             full_name     varchar(120),
                             gender        gender_type,
                             dob           date,
                             height_cm     numeric(5,2) CHECK (height_cm IS NULL OR height_cm BETWEEN 50 AND 250),
                             weight_kg     numeric(5,2) CHECK (weight_kg IS NULL OR weight_kg BETWEEN 20 AND 400),
                             bmi           numeric(5,2),
                             health_notes  text,
                             fitness_level varchar(16) CHECK (fitness_level IS NULL OR fitness_level IN ('BEGINNER','INTERMEDIATE','ADVANCED')),
                             target_goal   varchar(16) CHECK (target_goal IS NULL OR target_goal IN ('WEIGHT_LOSS', 'MAINTAIN', 'MUSCLE_GAIN')),
                             avatar_url    text,
                             timezone      varchar(64) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh',
                             unit_weight   varchar(8) NOT NULL DEFAULT 'kg' CHECK (unit_weight IN ('kg','lb')),
                             unit_length   varchar(8) NOT NULL DEFAULT 'cm' CHECK (unit_length IN ('cm','in')),
                             object_key    varchar(255),
                             created_at    timestamp NOT NULL DEFAULT now(),
                             updated_at    timestamp NOT NULL DEFAULT now(),
                             version       int NOT NULL DEFAULT 0,
                             is_deleted    boolean NOT NULL DEFAULT false,
                             deleted_at    timestamptz,
                             created_by    uuid,
                             updated_by    uuid
);

CREATE TRIGGER trg_user_profiles_updated_at BEFORE UPDATE ON user_profiles FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_user_profiles_version BEFORE UPDATE ON user_profiles FOR EACH ROW EXECUTE FUNCTION bump_version();

-- User measurements
CREATE TABLE user_measurements (
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

-- Refresh tokens
CREATE TABLE refresh_tokens (
                              id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                              user_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              token_hash   text NOT NULL,
                              issued_at    timestamp NOT NULL DEFAULT now(),
                              expires_at   timestamp NOT NULL,
                              revoked_at   timestamp,
                              user_agent   text,
                              ip           varchar(45),
                              UNIQUE (user_id, token_hash)
);

-- Verification tokens
CREATE TABLE verification_tokens (
                                   id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                   user_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                   type         verification_type NOT NULL,
                                   token_hash   text NOT NULL,
                                   expires_at   timestamp NOT NULL,
                                   consumed_at  timestamp,
                                   created_at   timestamp NOT NULL DEFAULT now(),
                                   UNIQUE (type, token_hash)
);

-- OAuth accounts
CREATE TABLE oauth_accounts (
                              id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                              user_id           uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              provider          varchar(32) NOT NULL,
                              provider_user_id  varchar(128) NOT NULL,
                              email             citext,
                              created_at        timestamp NOT NULL DEFAULT now(),
                              UNIQUE (provider, provider_user_id)
);

-- Login attempts
CREATE TABLE login_attempts (
                              id           bigserial PRIMARY KEY,
                              user_id      uuid REFERENCES users(id) ON DELETE SET NULL,
                              email        citext,
                              success      boolean NOT NULL,
                              ip           varchar(45),
                              user_agent   text,
                              occurred_at  timestamp NOT NULL DEFAULT now()
);

-- Goals
CREATE TABLE goals (
                     id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                     user_id            uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                     objective          objective_type NOT NULL,
                     sessions_per_week  int NOT NULL CHECK (sessions_per_week BETWEEN 1 AND 14),
                     session_minutes    int NOT NULL CHECK (session_minutes BETWEEN 10 AND 180),
                     preferences        jsonb,
                     status             goal_status,
                     started_at         date NOT NULL DEFAULT current_date,
                     ended_at           date,
                     created_at         timestamp NOT NULL DEFAULT now(),
                     updated_at         timestamptz NOT NULL DEFAULT now(),
                     version            int NOT NULL DEFAULT 0,
                     is_deleted         boolean NOT NULL DEFAULT false,
                     deleted_at         timestamptz,
                     created_by         uuid,
                     updated_by         uuid,
                     CONSTRAINT goals_end_ge_start CHECK (ended_at IS NULL OR ended_at >= started_at)
);

CREATE TRIGGER trg_goals_updated_at BEFORE UPDATE ON goals FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_goals_version BEFORE UPDATE ON goals FOR EACH ROW EXECUTE FUNCTION bump_version();

-- Body parts
CREATE TABLE body_parts (
                          code        varchar(32) PRIMARY KEY,
                          name        varchar(64) NOT NULL,
                          image_url   text,
                          created_at  timestamp NOT NULL DEFAULT now(),
                          updated_at  timestamp NOT NULL DEFAULT now()
);

-- Muscles
CREATE TABLE muscles (
                       code varchar(32) PRIMARY KEY,
                       name varchar(64) NOT NULL,
                       body_part varchar(32) REFERENCES body_parts(code) ON DELETE SET NULL
);

-- Equipment types
CREATE TABLE equipments (
                               code varchar(32) PRIMARY KEY,
                               name varchar(64) NOT NULL
);

-- Exercise categories
CREATE TABLE exercise_categories (
                                   code        varchar(32) PRIMARY KEY,
                                   name        varchar(64) NOT NULL,
                                   image_url   text
);

-- Exercises
CREATE TABLE exercises (
                         id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                         slug             varchar(80) NOT NULL UNIQUE,
                         name             varchar(120) NOT NULL,
                         primary_muscle   varchar(32) REFERENCES muscles(code) ON DELETE RESTRICT,
                         equipment        varchar(32) REFERENCES equipments(code) ON DELETE RESTRICT,
                         instructions     text,
                         safety_notes     text,
                         thumbnail_url    text,
                         body_part        varchar(32),
                         exercise_category varchar(32) NOT NULL REFERENCES exercise_categories(code),
                         created_at       timestamp NOT NULL DEFAULT now(),
                         updated_at       timestamptz NOT NULL DEFAULT now(),
                         version          int NOT NULL DEFAULT 0,
                         is_deleted       boolean NOT NULL DEFAULT false,
                         deleted_at       timestamptz,
                         created_by       uuid,
                         updated_by       uuid
);

CREATE TRIGGER trg_exercises_updated_at BEFORE UPDATE ON exercises FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_exercises_version BEFORE UPDATE ON exercises FOR EACH ROW EXECUTE FUNCTION bump_version();

-- Exercise muscles
CREATE TABLE exercise_muscles (
                                exercise_id uuid NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
                                muscle_code varchar(32) NOT NULL REFERENCES muscles(code) ON DELETE RESTRICT,
                                role        muscle_role NOT NULL DEFAULT 'SECONDARY',
                                PRIMARY KEY (exercise_id, muscle_code)
);

-- Exercise equipment
CREATE TABLE exercise_equipments (
                                  exercise_id   uuid NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
                                  equipment_code varchar(32) NOT NULL REFERENCES equipments(code) ON DELETE RESTRICT,
                                  PRIMARY KEY (exercise_id, equipment_code)
);

-- Plans
CREATE TABLE plans (
                     id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                     user_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                     goal_id      uuid REFERENCES goals(id) ON DELETE SET NULL,
                     title        varchar(120),
                     source       plan_source_type NOT NULL DEFAULT 'AI',
                     cycle_weeks  int NOT NULL DEFAULT 4 CHECK (cycle_weeks BETWEEN 1 AND 16),
                     status       plan_status_type NOT NULL DEFAULT 'ACTIVE',
                     created_at   timestamp NOT NULL DEFAULT now(),
                     updated_at   timestamp NOT NULL DEFAULT now(),
                     version      int NOT NULL DEFAULT 0
);

CREATE TRIGGER trg_plans_updated_at BEFORE UPDATE ON plans FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_plans_version BEFORE UPDATE ON plans FOR EACH ROW EXECUTE FUNCTION bump_version();

-- Plan days
CREATE TABLE plan_days (
                         id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                         plan_id        uuid NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
                         day_index      int NOT NULL CHECK (day_index >= 1),
                         split_name     varchar(50),
                         scheduled_date date,
                         created_at     timestamp NOT NULL DEFAULT now(),
                         UNIQUE (plan_id, day_index)
);

-- Plan items
CREATE TABLE plan_items (
                          id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                          plan_day_id  uuid NOT NULL REFERENCES plan_days(id) ON DELETE CASCADE,
                          exercise_id  uuid NOT NULL REFERENCES exercises(id) ON DELETE RESTRICT,
                          item_index   int  NOT NULL CHECK (item_index >= 1),
                          prescription jsonb NOT NULL,
                          notes        text,
                          created_at   timestamp NOT NULL DEFAULT now(),
                          UNIQUE (plan_day_id, item_index),
                          CONSTRAINT prescription_is_object CHECK (jsonb_typeof(prescription) = 'object'),
                          CONSTRAINT prescription_sets_chk CHECK ((prescription ? 'sets') AND ((prescription->>'sets')::int >= 1))
);

-- Sessions
CREATE TABLE sessions (
                        id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                        user_id       uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                        plan_day_id   uuid REFERENCES plan_days(id) ON DELETE SET NULL,
                        started_at    timestamp NOT NULL DEFAULT now(),
                        ended_at      timestamp,
                        status        session_status NOT NULL DEFAULT 'IN_PROGRESS',
                        session_rpe   numeric(3,1) CHECK (session_rpe IS NULL OR (session_rpe BETWEEN 0 AND 10)),
                        notes         text,
                        created_at    timestamp NOT NULL DEFAULT now(),
                        updated_at    timestamptz NOT NULL DEFAULT now(),
                        version       int NOT NULL DEFAULT 0,
                        is_deleted    boolean NOT NULL DEFAULT false,
                        deleted_at    timestamptz,
                        created_by    uuid,
                        updated_by    uuid,
                        CONSTRAINT sessions_end_ge_start CHECK (ended_at IS NULL OR ended_at >= started_at)
);

CREATE TRIGGER trg_sessions_updated_at BEFORE UPDATE ON sessions FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_sessions_version BEFORE UPDATE ON sessions FOR EACH ROW EXECUTE FUNCTION bump_version();

-- Session sets
CREATE TABLE session_sets (
                            id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                            session_id   uuid NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
                            exercise_id  uuid NOT NULL REFERENCES exercises(id) ON DELETE RESTRICT,
                            set_index    int NOT NULL CHECK (set_index >= 1),
                            planned      jsonb,
                            actual       jsonb,
                            created_at   timestamp NOT NULL DEFAULT now(),
                            updated_at   timestamptz NOT NULL DEFAULT now(),
                            version      int NOT NULL DEFAULT 0,
                            is_deleted   boolean NOT NULL DEFAULT false,
                            deleted_at   timestamptz,
                            created_by   uuid,
                            updated_by   uuid,
                            UNIQUE (session_id, exercise_id, set_index),
                            CONSTRAINT actual_obj_chk CHECK (actual IS NULL OR jsonb_typeof(actual)='object'),
                            CONSTRAINT actual_rpe_chk CHECK (actual IS NULL OR ((actual ? 'rpe') = false OR ((actual->>'rpe')::numeric BETWEEN 0 AND 10)))
);

CREATE TRIGGER trg_session_sets_updated_at BEFORE UPDATE ON session_sets FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_session_sets_version BEFORE UPDATE ON session_sets FOR EACH ROW EXECUTE FUNCTION bump_version();

-- Device tokens
CREATE TABLE device_tokens (
                             id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                             user_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                             platform     device_platform NOT NULL,
                             push_token   text NOT NULL,
                             enabled      boolean NOT NULL DEFAULT true,
                             created_at   timestamp NOT NULL DEFAULT now(),
                             UNIQUE (user_id, platform, push_token)
);

-- Notifications
CREATE TABLE notifications (
                             id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                             user_id       uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                             kind          notification_kind NOT NULL,
                             scheduled_at  timestamp NOT NULL,
                             status        notification_status NOT NULL DEFAULT 'SCHEDULED',
                             payload       jsonb,
                             created_at    timestamp NOT NULL DEFAULT now()
);

-- Chatbot
CREATE TABLE chatbot_threads (
                               id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                               user_id     uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               created_at  timestamp NOT NULL DEFAULT now()
);

CREATE TABLE chatbot_messages (
                                id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                thread_id   uuid NOT NULL REFERENCES chatbot_threads(id) ON DELETE CASCADE,
                                user_id     uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                role        varchar(16) NOT NULL CHECK (role IN ('USER','ASSISTANT','SYSTEM')),
                                content     text NOT NULL,
                                created_at  timestamp NOT NULL DEFAULT now()
);

-- Reports cache
CREATE TABLE reports_cache (
                             id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                             user_id       uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                             scope         varchar(16) NOT NULL CHECK (scope IN ('week','month')),
                             period_start  date NOT NULL,
                             payload       jsonb NOT NULL,
                             generated_at  timestamp NOT NULL DEFAULT now(),
                             UNIQUE (user_id, scope, period_start)
);

-- Subscriptions
CREATE TABLE subscriptions (
                             id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                             user_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                             tier         subscription_tier NOT NULL DEFAULT 'FREE',
                             platform     subscription_platform NOT NULL,
                             status       subscription_status NOT NULL DEFAULT 'ACTIVE',
                             started_at   timestamp NOT NULL DEFAULT now(),
                             expires_at   timestamp,
                             cancelled_at timestamp,
                             created_at   timestamp NOT NULL DEFAULT now(),
                             updated_at   timestamp NOT NULL DEFAULT now(),
                             version      int NOT NULL DEFAULT 0,
                             is_deleted   boolean NOT NULL DEFAULT false,
                             deleted_at   timestamptz,
                             created_by   uuid,
                             updated_by   uuid
);

CREATE TRIGGER trg_subscriptions_updated_at BEFORE UPDATE ON subscriptions FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_subscriptions_version BEFORE UPDATE ON subscriptions FOR EACH ROW EXECUTE FUNCTION bump_version();

-- Payments
CREATE TABLE payments (
                        id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                        user_id          uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                        provider         varchar(16) NOT NULL CHECK (provider IN ('stripe','apple','google')),
                        provider_txn_id  varchar(128) NOT NULL,
                        amount_cents     int NOT NULL CHECK (amount_cents >= 0),
                        currency         varchar(8) NOT NULL DEFAULT 'USD',
                        status           varchar(16) NOT NULL CHECK (status IN ('PENDING','SUCCEEDED','FAILED','REFUNDED')),
                        meta             jsonb,
                        created_at       timestamp NOT NULL DEFAULT now(),
                        updated_at       timestamp NOT NULL DEFAULT now(),
                        UNIQUE (provider, provider_txn_id)
);

CREATE TRIGGER trg_payments_updated_at BEFORE UPDATE ON payments FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Features
CREATE TABLE features (
                        key        varchar(64) PRIMARY KEY,
                        name       varchar(120) NOT NULL,
                        pro_only   boolean NOT NULL DEFAULT true
);

-- Entitlements
CREATE TABLE entitlements (
                            id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                            user_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            feature_key  varchar(64) NOT NULL REFERENCES features(key) ON DELETE RESTRICT,
                            limits       jsonb,
                            source       varchar(16) NOT NULL DEFAULT 'subscription' CHECK (source IN ('subscription','promo','gift','admin')),
                            starts_at    timestamp NOT NULL DEFAULT now(),
                            expires_at   timestamp,
                            created_at   timestamp NOT NULL DEFAULT now()
);

-- Nutrition
CREATE TABLE nutrition_targets (
                                 id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                 user_id       uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                 goal_id       uuid REFERENCES goals(id) ON DELETE SET NULL,
                                 calories_kcal int NOT NULL CHECK (calories_kcal BETWEEN 800 AND 5000),
                                 protein_g     int NOT NULL CHECK (protein_g BETWEEN 30 AND 400),
                                 fat_g         int NOT NULL CHECK (fat_g BETWEEN 20 AND 200),
                                 carbs_g       int NOT NULL CHECK (carbs_g BETWEEN 0 AND 800),
                                 created_at    timestamp NOT NULL DEFAULT now(),
                                 UNIQUE (user_id, goal_id)
);

CREATE TABLE meal_plans (
                          id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                          user_id     uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                          plan_date   date NOT NULL,
                          meta        jsonb,
                          created_at  timestamp NOT NULL DEFAULT now(),
                          UNIQUE (user_id, plan_date)
);

-- Challenges
CREATE TABLE challenges (
                          id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                          title       varchar(120) NOT NULL,
                          start_date  date NOT NULL,
                          end_date    date NOT NULL,
                          rules       jsonb,
                          created_at  timestamp NOT NULL DEFAULT now(),
                          CONSTRAINT challenges_end_ge_start CHECK (end_date >= start_date)
);

CREATE TABLE challenge_participants (
                                      challenge_id uuid NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
                                      user_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                      joined_at    timestamp NOT NULL DEFAULT now(),
                                      progress     jsonb,
                                      PRIMARY KEY (challenge_id, user_id)
);

-- Indexes
CREATE INDEX idx_users_email_not_deleted ON users(email) WHERE is_deleted = false;
CREATE INDEX idx_users_not_deleted ON users(id) WHERE is_deleted = false;
CREATE INDEX idx_measurements_user_time ON user_measurements(user_id, measured_at DESC);
CREATE INDEX idx_refresh_user_active ON refresh_tokens(user_id) WHERE revoked_at IS NULL;
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_vt_user_type ON verification_tokens(user_id, type);
CREATE INDEX idx_verification_tokens_expires_at ON verification_tokens(expires_at);
CREATE INDEX idx_oauth_user ON oauth_accounts(user_id);
CREATE INDEX idx_login_email_time ON login_attempts(email, occurred_at DESC);
CREATE INDEX idx_login_user_time ON login_attempts(user_id, occurred_at DESC);
CREATE INDEX idx_goals_user ON goals(user_id);
CREATE INDEX idx_goals_not_deleted ON goals(id) WHERE is_deleted = false;
CREATE UNIQUE INDEX uq_goal_active_per_user ON goals(user_id) WHERE ended_at IS NULL;
CREATE INDEX idx_exercises_primary_muscle ON exercises(primary_muscle);
CREATE INDEX idx_exercises_equipment ON exercises(equipment);
CREATE INDEX idx_exercises_category ON exercises(exercise_category);
CREATE INDEX idx_exercises_slug_not_deleted ON exercises(slug) WHERE is_deleted = false;
CREATE INDEX idx_exercises_not_deleted ON exercises(id) WHERE is_deleted = false;
CREATE INDEX idx_exmu_muscle ON exercise_muscles(muscle_code);
CREATE INDEX idx_exmu_role ON exercise_muscles(role);
CREATE INDEX idx_exeq_equipment ON exercise_equipments(equipment_code);
CREATE INDEX idx_plans_user_status ON plans(user_id, status);
CREATE INDEX idx_plan_days_plan_date ON plan_days(plan_id, scheduled_date);
CREATE INDEX idx_plan_items_prescription_gin ON plan_items USING GIN (prescription);
CREATE INDEX idx_sessions_user_time ON sessions(user_id, started_at DESC);
CREATE INDEX idx_sessions_plan_day ON sessions(plan_day_id);
CREATE INDEX idx_sessions_user_status_time ON sessions(user_id, status, started_at DESC);
CREATE INDEX idx_sessions_not_deleted ON sessions(id) WHERE is_deleted = false;
CREATE INDEX idx_session_sets_session ON session_sets(session_id);
CREATE INDEX idx_session_sets_actual_gin ON session_sets USING GIN (actual);
CREATE INDEX idx_notifications_user_time ON notifications(user_id, scheduled_at);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_user_status_time ON notifications(user_id, status, scheduled_at);
CREATE INDEX idx_chat_threads_user ON chatbot_threads(user_id, created_at DESC);
CREATE INDEX idx_chatbot_user_time ON chatbot_messages(user_id, created_at);
CREATE INDEX idx_payments_user_time ON payments(user_id, created_at DESC);
CREATE INDEX idx_entitlements_user_feature ON entitlements(user_id, feature_key);
CREATE INDEX idx_entitlements_expires_at ON entitlements(user_id, expires_at);
CREATE INDEX idx_meal_plans_user_date ON meal_plans(user_id, plan_date);
CREATE UNIQUE INDEX uq_subscription_active_per_user ON subscriptions(user_id) WHERE status IN ('ACTIVE','TRIAL');

-- Seed data
INSERT INTO body_parts (code, name) VALUES
                                      ('neck', 'Neck'),
                                      ('lower_arms', 'Lower Arms'),
                                      ('shoulders', 'Shoulders'),
                                      ('cardio', 'Cardio'),
                                      ('upper_arms', 'Upper Arms'),
                                      ('chest', 'Chest'),
                                      ('lower_legs', 'Lower Legs'),
                                      ('back', 'Back'),
                                      ('upper_legs', 'Upper Legs'),
                                      ('waist', 'Waist'),
                                      ('calves', 'Calves'),
                                      ('glutes', 'Glutes');

INSERT INTO exercise_categories (code, name, image_url) VALUES
                                                          ('strength', 'Strength', 'https://cdn.exercisedb.dev/exercisetypes/strength.webp'),
                                                          ('cardio', 'Cardio', 'https://cdn.exercisedb.dev/exercisetypes/cardio.webp'),
                                                          ('plyometrics', 'Plyometrics', 'https://cdn.exercisedb.dev/exercisetypes/plyometrics.webp'),
                                                          ('stretching', 'Stretching', 'https://cdn.exercisedb.dev/exercisetypes/stretching.webp'),
                                                          ('weightlifting', 'Weightlifting', 'https://cdn.exercisedb.dev/exercisetypes/weightlifting.webp'),
                                                          ('yoga', 'Yoga', 'https://cdn.exercisedb.dev/exercisetypes/yoga.webp'),
                                                          ('aerobic', 'Aerobic', 'https://cdn.exercisedb.dev/exercisetypes/aerobic.webp');

INSERT INTO features(key, name, pro_only) VALUES
                                            ('export_pdf', 'Export reports as PDF', true),
                                            ('advanced_reports', 'Advanced progress analytics', true),
                                            ('nutrition', 'Meal planning & targets', true),
                                            ('offline_video', 'Offline video cache', true);

INSERT INTO equipments(code,name) VALUES

                                         ('stepmill_machine', 'Stepmill Machine'),
                                         ('elliptical_machine', 'Elliptical Machine'),
                                         ('trap_bar', 'Trap Bar'),
                                         ('tire', 'Tire'),
                                         ('stationary_bike', 'Stationary Bike'),
                                         ('wheel_roller', 'Wheel Roller'),
                                         ('smith_machine', 'Smith Machine'),
                                         ('hammer', 'Hammer'),
                                         ('skierg_machine', 'Skierg Machine'),
                                         ('roller', 'Roller'),
                                         ('resistance_band', 'Resistance Band'),
                                         ('bosu_ball', 'Bosu Ball'),
                                         ('weighted', 'Weighted'),
                                         ('olympic_barbell', 'Olympic Barbell'),
                                         ('kettlebell', 'Kettlebell'),
                                         ('upper_body_ergometer', 'Upper Body Ergometer'),
                                         ('sled_machine', 'Sled Machine'),
                                         ('ez_barbell', 'Ez Barbell'),
                                         ('dumbbell', 'Dumbbell'),
                                         ('rope', 'Rope'),
                                         ('barbell', 'Barbell'),
                                         ('band', 'Band'),
                                         ('stability_ball', 'Stability Ball'),
                                         ('medicine_ball', 'Medicine Ball'),
                                         ('assisted', 'Assisted'),
                                         ('leverage_machine', 'Leverage Machine'),
                                         ('cable', 'Cable'),
                                         ('body_weight', 'Body Weight')
ON CONFLICT (code) DO NOTHING;

INSERT INTO muscles(code,name) VALUES
                                 ('shins', 'Shins'),
                                 ('hands', 'Hands'),
                                 ('sternocleidomastoid', 'Sternocleidomastoid'),
                                 ('soleus', 'Soleus'),
                                 ('inner_thighs', 'Inner Thighs'),
                                 ('lower_abs', 'Lower Abs'),
                                 ('grip_muscles', 'Grip Muscles'),
                                 ('abdominals', 'Abdominals'),
                                 ('wrist_extensors', 'Wrist Extensors'),
                                 ('wrist_flexors', 'Wrist Flexors'),
                                 ('latissimus_dorsi', 'Latissimus Dorsi'),
                                 ('upper_chest', 'Upper Chest'),
                                 ('rotator_cuff', 'Rotator Cuff'),
                                 ('wrists', 'Wrists'),
                                 ('groin', 'Groin'),
                                 ('brachialis', 'Brachialis'),
                                 ('deltoids', 'Deltoids'),
                                 ('feet', 'Feet'),
                                 ('ankles', 'Ankles'),
                                 ('trapezius', 'Trapezius'),
                                 ('rear_deltoids', 'Rear Deltoids'),
                                 ('chest', 'Chest'),
                                 ('quadriceps', 'Quadriceps'),
                                 ('back', 'Back'),
                                 ('core', 'Core'),
                                 ('shoulders', 'Shoulders'),
                                 ('ankle_stabilizers', 'Ankle Stabilizers'),
                                 ('rhomboids', 'Rhomboids'),
                                 ('obliques', 'Obliques'),
                                 ('lower_back', 'Lower Back'),
                                 ('hip_flexors', 'Hip Flexors'),
                                 ('levator_scapulae', 'Levator Scapulae'),
                                 ('abductors', 'Abductors'),
                                 ('serratus_anterior', 'Serratus Anterior'),
                                 ('traps', 'Traps'),
                                 ('forearms', 'Forearms'),
                                 ('delts', 'Delts'),
                                 ('biceps', 'Biceps'),
                                 ('upper_back', 'Upper Back'),
                                 ('spine', 'Spine'),
                                 ('cardiovascular_system', 'Cardiovascular System'),
                                 ('triceps', 'Triceps'),
                                 ('adductors', 'Adductors'),
                                 ('hamstrings', 'Hamstrings'),
                                 ('glutes', 'Glutes'),
                                 ('pectorals', 'Pectorals'),
                                 ('calves', 'Calves'),
                                 ('lats', 'Lats'),
                                 ('quads', 'Quads'),
                                 ('abs', 'Abs')

ON CONFLICT (code) DO NOTHING;
