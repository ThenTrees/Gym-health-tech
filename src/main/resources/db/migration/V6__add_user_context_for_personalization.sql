
-- V8__add_user_context_for_personalization.sql
-- Thêm context cho personalized recommendations

-- Bảng lưu user exercise preferences và feedback
CREATE TABLE user_exercise_preferences (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                         exercise_id UUID NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
                                         preference_score NUMERIC(3,2) CHECK (preference_score BETWEEN -1 AND 1), -- -1 dislike, 0 neutral, 1 like
                                         difficulty_rating NUMERIC(3,1) CHECK (difficulty_rating BETWEEN 1 AND 5),
                                         notes TEXT,
                                         created_at TIMESTAMP DEFAULT NOW(),
                                         updated_at TIMESTAMP DEFAULT NOW(),

                                         CONSTRAINT uq_user_exercise_pref UNIQUE (user_id, exercise_id)
);

CREATE TRIGGER trg_user_exercise_preferences_updated_at
  BEFORE UPDATE ON user_exercise_preferences
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Indexes
CREATE INDEX idx_user_exercise_prefs_user ON user_exercise_preferences(user_id);
CREATE INDEX idx_user_exercise_prefs_exercise ON user_exercise_preferences(exercise_id);
CREATE INDEX idx_user_exercise_prefs_score ON user_exercise_preferences(preference_score);

-- Bảng lưu workout plan feedback để improve AI
CREATE TABLE plan_feedback (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             plan_id UUID NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
                             user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                             overall_rating NUMERIC(2,1) CHECK (overall_rating BETWEEN 1 AND 5),
                             difficulty_rating NUMERIC(2,1) CHECK (difficulty_rating BETWEEN 1 AND 5),
                             enjoyment_rating NUMERIC(2,1) CHECK (enjoyment_rating BETWEEN 1 AND 5),
                             feedback_text TEXT,
                             created_at TIMESTAMP DEFAULT NOW(),

                             CONSTRAINT uq_plan_feedback UNIQUE (plan_id, user_id)
);

-- Indexes
CREATE INDEX idx_plan_feedback_plan ON plan_feedback(plan_id);
CREATE INDEX idx_plan_feedback_user ON plan_feedback(user_id);
CREATE INDEX idx_plan_feedback_ratings ON plan_feedback(overall_rating, difficulty_rating, enjoyment_rating);
