-- V12__add_rag_search_optimization.sql
-- Optimized RAG search performance with scheduled refresh strategy
-- CRITICAL: Uses manual/scheduled refresh instead of auto-refresh triggers

-- Drop old structures if they exist (for safe re-running)
DROP TRIGGER IF EXISTS trg_refresh_exercise_search_exercises ON exercises;
DROP TRIGGER IF EXISTS trg_refresh_exercise_search_analytics ON exercise_usage_analytics;
DROP FUNCTION IF EXISTS refresh_exercise_search_view() CASCADE;
DROP MATERIALIZED VIEW IF EXISTS exercise_search_view CASCADE;

-- Create optimized materialized view with weighted text search
CREATE MATERIALIZED VIEW exercise_search_view AS
SELECT
  e.id,
  e.slug,
  e.name,
  e.primary_muscle,
  e.equipment,
  e.body_part,
  e.exercise_category,
  e.exercise_type,
  e.difficulty_level,
  e.instructions,
  e.benefits,
  e.contraindications,
  e.safety_notes,
  e.tags,
  e.alternative_names,
  e.thumbnail_url,
  m.name as muscle_name,
  bp.name as body_part_name,
  eq.name as equipment_name,
  ec.name as category_name,
  -- Weighted tsvector for better search ranking
  -- A = highest priority, D = lowest priority
  setweight(to_tsvector('english', COALESCE(e.name, '')), 'A') ||
  setweight(to_tsvector('english', COALESCE(array_to_string(e.alternative_names, ' '), '')), 'A') ||
  setweight(to_tsvector('english', COALESCE(m.name, '')), 'B') ||
  setweight(to_tsvector('english', COALESCE(e.benefits, '')), 'B') ||
  setweight(to_tsvector('english', COALESCE(e.instructions, '')), 'C') ||
  setweight(to_tsvector('english', COALESCE(bp.name, '')), 'C') ||
  setweight(to_tsvector('english', COALESCE(eq.name, '')), 'C') ||
  setweight(to_tsvector('english', COALESCE(ec.name, '')), 'C') ||
  setweight(to_tsvector('english', COALESCE(array_to_string(e.tags, ' '), '')), 'D')
    as search_vector,
  -- Analytics scores for ranking
  COALESCE(ua.avg_user_rating, 2.5) as avg_rating,
  COALESCE(ua.success_rate, 0.5) as success_rate,
  COALESCE(ua.used_in_plans_count, 0) as popularity_score,
  -- Track when view was last refreshed
  NOW() as last_refreshed
FROM exercises e
       LEFT JOIN muscles m ON e.primary_muscle = m.code
       LEFT JOIN body_parts bp ON e.body_part = bp.code
       LEFT JOIN equipments eq ON e.equipment = eq.code
       LEFT JOIN exercise_categories ec ON e.exercise_category = ec.code
       LEFT JOIN exercise_usage_analytics ua ON e.id = ua.exercise_id
WHERE e.is_deleted = false;

-- Create indexes for fast filtering and searching
CREATE UNIQUE INDEX idx_exercise_search_view_id
  ON exercise_search_view(id);

CREATE INDEX idx_exercise_search_view_muscle
  ON exercise_search_view(primary_muscle)
  WHERE primary_muscle IS NOT NULL;

CREATE INDEX idx_exercise_search_view_equipment
  ON exercise_search_view(equipment)
  WHERE equipment IS NOT NULL;

CREATE INDEX idx_exercise_search_view_body_part
  ON exercise_search_view(body_part)
  WHERE body_part IS NOT NULL;

CREATE INDEX idx_exercise_search_view_category
  ON exercise_search_view(exercise_category);

CREATE INDEX idx_exercise_search_view_type
  ON exercise_search_view(exercise_type)
  WHERE exercise_type IS NOT NULL;

CREATE INDEX idx_exercise_search_view_difficulty
  ON exercise_search_view(difficulty_level);

CREATE INDEX idx_exercise_search_view_rating
  ON exercise_search_view(avg_rating DESC);

CREATE INDEX idx_exercise_search_view_popularity
  ON exercise_search_view(popularity_score DESC);

-- Composite indexes for common filter combinations
CREATE INDEX idx_exercise_search_view_muscle_difficulty
  ON exercise_search_view(primary_muscle, difficulty_level);

CREATE INDEX idx_exercise_search_view_equipment_difficulty
  ON exercise_search_view(equipment, difficulty_level);

-- GIN index on pre-computed tsvector (much faster than runtime to_tsvector)
CREATE INDEX idx_exercise_search_vector_gin
  ON exercise_search_view USING GIN (search_vector);

-- Function for manual refresh (to be called by scheduler or on-demand)
CREATE OR REPLACE FUNCTION refresh_exercise_search_view() RETURNS VOID AS $$
BEGIN
  -- Use CONCURRENTLY to avoid blocking reads
  REFRESH MATERIALIZED VIEW CONCURRENTLY exercise_search_view;

  RAISE NOTICE 'Exercise search view refreshed successfully at %', NOW();
EXCEPTION WHEN OTHERS THEN
  RAISE WARNING 'Failed to refresh exercise search view: %', SQLERRM;
  RAISE;
END;
$$ LANGUAGE plpgsql;

-- Function to check view freshness
CREATE OR REPLACE FUNCTION check_search_view_freshness()
  RETURNS TABLE(
                 last_refresh TIMESTAMP,
                 age_minutes NUMERIC,
                 is_stale BOOLEAN,
                 recommendation TEXT
               ) AS $$
DECLARE
  refresh_time TIMESTAMP;
  age_mins NUMERIC;
BEGIN
  -- Get last refresh time
  SELECT last_refreshed INTO refresh_time
  FROM exercise_search_view
  LIMIT 1;

  IF refresh_time IS NULL THEN
    RETURN QUERY SELECT
                   NULL::TIMESTAMP,
                   NULL::NUMERIC,
                   TRUE,
                   'View has never been refreshed'::TEXT;
    RETURN;
  END IF;

  -- Calculate age in minutes
  age_mins := EXTRACT(EPOCH FROM (NOW() - refresh_time)) / 60;

  RETURN QUERY SELECT
                 refresh_time,
                 ROUND(age_mins, 2),
                 age_mins > 240, -- Stale if older than 4 hours
                 CASE
                   WHEN age_mins > 240 THEN 'STALE: Recommend refreshing immediately'
                   WHEN age_mins > 120 THEN 'AGING: Consider refreshing soon'
                   ELSE 'FRESH: No action needed'
                   END::TEXT;
END;
$$ LANGUAGE plpgsql;

-- Function to get search statistics
CREATE OR REPLACE FUNCTION get_search_view_stats()
  RETURNS TABLE(
                 total_exercises INTEGER,
                 by_difficulty JSONB,
                 by_equipment JSONB,
                 by_muscle JSONB,
                 avg_popularity NUMERIC,
                 last_refresh TIMESTAMP
               ) AS $$
BEGIN
  RETURN QUERY
    SELECT
      COUNT(*)::INTEGER as total,
      jsonb_object_agg(
        COALESCE(difficulty_level::TEXT, 'unknown'),
        count
      ) as by_diff,
      (
        SELECT jsonb_object_agg(equipment_name, cnt)
        FROM (
               SELECT equipment_name, COUNT(*) as cnt
               FROM exercise_search_view
               WHERE equipment_name IS NOT NULL
               GROUP BY equipment_name
               ORDER BY cnt DESC
               LIMIT 10
             ) eq
      ) as by_eq,
      (
        SELECT jsonb_object_agg(muscle_name, cnt)
        FROM (
               SELECT muscle_name, COUNT(*) as cnt
               FROM exercise_search_view
               WHERE muscle_name IS NOT NULL
               GROUP BY muscle_name
               ORDER BY cnt DESC
               LIMIT 10
             ) ms
      ) as by_mus,
      ROUND(AVG(popularity_score), 2) as avg_pop,
      MAX(last_refreshed) as last_ref
    FROM (
           SELECT
             difficulty_level,
             COUNT(*) as count
           FROM exercise_search_view
           GROUP BY difficulty_level
         ) diff_counts,
         exercise_search_view;
END;
$$ LANGUAGE plpgsql;

-- Add helpful comments
COMMENT ON MATERIALIZED VIEW exercise_search_view IS
  'Optimized view for exercise search with pre-computed tsvector and analytics.
   Refresh strategy: Manual or scheduled (every 1-4 hours recommended).
   To refresh: SELECT refresh_exercise_search_view();
   To check freshness: SELECT * FROM check_search_view_freshness();
   DO NOT add auto-refresh triggers - they kill performance!';

COMMENT ON FUNCTION refresh_exercise_search_view() IS
  'Manually refresh the exercise search materialized view.
   Use CONCURRENTLY to avoid blocking reads.
   Schedule this to run every 1-4 hours via pg_cron or application scheduler.
   Example cron: SELECT cron.schedule(''refresh-search'', ''0 */2 * * *'', ''SELECT refresh_exercise_search_view()'');';

COMMENT ON FUNCTION check_search_view_freshness() IS
  'Check how old the materialized view data is.
   Returns: last_refresh timestamp, age in minutes, staleness flag, and recommendation.
   Example: SELECT * FROM check_search_view_freshness();';

COMMENT ON FUNCTION get_search_view_stats() IS
  'Get statistics about exercises in the search view.
   Returns counts by difficulty, equipment, muscle, and refresh info.
   Example: SELECT * FROM get_search_view_stats();';

-- Perform initial refresh
REFRESH MATERIALIZED VIEW exercise_search_view;

-- Analyze for query optimizer
ANALYZE exercise_search_view;

-- Log completion
DO $$
  BEGIN
    RAISE NOTICE '============================================';
    RAISE NOTICE 'V12 Migration completed successfully';
    RAISE NOTICE '============================================';
    RAISE NOTICE 'Materialized view created: exercise_search_view';
    RAISE NOTICE 'Total exercises indexed: %', (SELECT COUNT(*) FROM exercise_search_view);
    RAISE NOTICE 'Last refreshed: %', (SELECT MAX(last_refreshed) FROM exercise_search_view);
    RAISE NOTICE '';
    RAISE NOTICE 'IMPORTANT: Setup scheduled refresh using one of these methods:';
    RAISE NOTICE '1. pg_cron: SELECT cron.schedule(''refresh-search'', ''0 */2 * * *'', ''SELECT refresh_exercise_search_view()'');';
    RAISE NOTICE '2. Application scheduler: Call refresh_exercise_search_view() every 1-4 hours';
    RAISE NOTICE '3. Manual: Run SELECT refresh_exercise_search_view() when needed';
    RAISE NOTICE '';
    RAISE NOTICE 'Check view status: SELECT * FROM check_search_view_freshness();';
    RAISE NOTICE '============================================';
  END $$;
