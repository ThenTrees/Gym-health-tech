
-- V11__add_exercise_analytics_for_rag.sql
-- Analytics để improve RAG recommendations

-- Bảng track exercise usage trong plans
CREATE TABLE exercise_usage_analytics (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                        exercise_id UUID NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
                                        used_in_plans_count INTEGER DEFAULT 0,
                                        avg_user_rating NUMERIC(3,2),
                                        success_rate NUMERIC(5,4), -- % users who complete this exercise
                                        last_updated TIMESTAMP DEFAULT NOW()
);

-- Indexes
CREATE UNIQUE INDEX uq_exercise_analytics ON exercise_usage_analytics(exercise_id);
CREATE INDEX idx_exercise_analytics_rating ON exercise_usage_analytics(avg_user_rating DESC);
CREATE INDEX idx_exercise_analytics_success ON exercise_usage_analytics(success_rate DESC);

-- Function để update analytics
CREATE OR REPLACE FUNCTION update_exercise_analytics()
RETURNS TRIGGER AS $$
BEGIN
INSERT INTO exercise_usage_analytics (exercise_id, used_in_plans_count, last_updated)
VALUES (NEW.exercise_id, 1, NOW())
  ON CONFLICT (exercise_id)
    DO UPDATE SET
  used_in_plans_count = exercise_usage_analytics.used_in_plans_count + 1,
           last_updated = NOW();

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger để auto-update khi có plan_item mới
CREATE TRIGGER trg_update_exercise_analytics
  AFTER INSERT ON plan_items
  FOR EACH ROW EXECUTE FUNCTION update_exercise_analytics();
