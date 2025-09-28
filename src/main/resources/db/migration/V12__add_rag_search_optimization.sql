
-- V12__add_rag_search_optimization.sql
-- Tối ưu hóa search performance cho RAG

-- Materialized view cho exercise search
CREATE MATERIALIZED VIEW exercise_search_view AS
SELECT
  e.id,
  e.slug,
  e.name,
  e.primary_muscle,
  e.equipment,
  e.body_part,
  e.exercise_category,
  e.difficulty_level,
  e.instructions,
  e.benefits,
  e.tags,
  e.alternative_names,
  m.name as muscle_name,
  bp.name as body_part_name,
  eq.name as equipment_name,
  ec.name as category_name,
  -- Tạo searchable content
  e.name || ' ' ||
  COALESCE(e.instructions, '') || ' ' ||
  COALESCE(e.benefits, '') || ' ' ||
  COALESCE(m.name, '') || ' ' ||
  COALESCE(bp.name, '') || ' ' ||
  COALESCE(eq.name, '') || ' ' ||
  COALESCE(ec.name, '') || ' ' ||
  COALESCE(array_to_string(e.tags, ' '), '') || ' ' ||
  COALESCE(array_to_string(e.alternative_names, ' '), '') as search_content,
  -- Analytics scores
  COALESCE(ua.avg_user_rating, 2.5) as avg_rating,
  COALESCE(ua.success_rate, 0.5) as success_rate,
  COALESCE(ua.used_in_plans_count, 0) as popularity_score
FROM exercises e
       LEFT JOIN muscles m ON e.primary_muscle = m.code
       LEFT JOIN body_parts bp ON e.body_part = bp.code
       LEFT JOIN equipments eq ON e.equipment = eq.code
       LEFT JOIN exercise_categories ec ON e.exercise_category = ec.code
       LEFT JOIN exercise_usage_analytics ua ON e.id = ua.exercise_id
WHERE e.is_deleted = false;

-- Indexes cho materialized view
CREATE UNIQUE INDEX idx_exercise_search_view_id ON exercise_search_view(id);
CREATE INDEX idx_exercise_search_view_muscle ON exercise_search_view(primary_muscle);
CREATE INDEX idx_exercise_search_view_equipment ON exercise_search_view(equipment);
CREATE INDEX idx_exercise_search_view_difficulty ON exercise_search_view(difficulty_level);
CREATE INDEX idx_exercise_search_view_rating ON exercise_search_view(avg_rating DESC);
CREATE INDEX idx_exercise_search_view_popularity ON exercise_search_view(popularity_score DESC);

-- Full text search index
CREATE INDEX idx_exercise_search_content_gin
  ON exercise_search_view USING GIN (to_tsvector('english', search_content));

-- Function để refresh materialized view
CREATE OR REPLACE FUNCTION refresh_exercise_search_view()
RETURNS TRIGGER AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY exercise_search_view;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Triggers để auto-refresh view khi data thay đổi
CREATE TRIGGER trg_refresh_exercise_search_exercises
  AFTER INSERT OR UPDATE OR DELETE ON exercises
  FOR EACH STATEMENT EXECUTE FUNCTION refresh_exercise_search_view();

CREATE TRIGGER trg_refresh_exercise_search_analytics
  AFTER INSERT OR UPDATE OR DELETE ON exercise_usage_analytics
  FOR EACH STATEMENT EXECUTE FUNCTION refresh_exercise_search_view();

-- Initial refresh
REFRESH MATERIALIZED VIEW exercise_search_view;
