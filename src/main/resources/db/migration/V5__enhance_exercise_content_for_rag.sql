-- V5__enhance_exercise_content_for_rag.sql
-- Tăng cường content cho exercises để RAG hiệu quả hơn

-- Thêm các cột content phong phú
ALTER TABLE exercises
  ADD COLUMN benefits TEXT,
ADD COLUMN contraindications TEXT,
ADD COLUMN tags TEXT[],
ADD COLUMN alternative_names TEXT[];

-- Index cho text search
CREATE INDEX idx_exercises_benefits_gin ON exercises USING GIN (to_tsvector('english', benefits));
CREATE INDEX idx_exercises_tags_gin ON exercises USING GIN (tags);
CREATE INDEX idx_exercises_alternatives_gin ON exercises USING GIN (alternative_names);

-- Full text search index cho exercise name và instructions
CREATE INDEX idx_exercises_fulltext ON exercises USING GIN (
  to_tsvector('english', name || ' ' || COALESCE(instructions, '') || ' ' || COALESCE(benefits, ''))
  );
