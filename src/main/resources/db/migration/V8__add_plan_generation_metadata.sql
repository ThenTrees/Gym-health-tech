-- V8__add_plan_generation_metadata.sql
-- Thêm metadata cho plan generation và AI context

-- Thêm cột AI metadata vào plans
ALTER TABLE plans
  ADD COLUMN ai_metadata JSONB DEFAULT '{}',
ADD COLUMN generation_params JSONB DEFAULT '{}';

-- Index cho AI metadata
CREATE INDEX idx_plans_ai_metadata ON plans USING GIN (ai_metadata);

-- Thêm similarity score vào plan_items (để track exercise selection quality)
ALTER TABLE plan_items
  ADD COLUMN similarity_score NUMERIC(5,4) CHECK (similarity_score IS NULL OR (similarity_score BETWEEN 0 AND 1));

-- Index cho similarity tracking
CREATE INDEX idx_plan_items_similarity ON plan_items(similarity_score) WHERE similarity_score IS NOT NULL;
