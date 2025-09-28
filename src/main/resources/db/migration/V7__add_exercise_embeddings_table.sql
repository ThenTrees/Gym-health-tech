-- V7__add_exercise_embeddings_table.sql
-- Tạo bảng lưu trữ embeddings cho RAG service

CREATE EXTENSION IF NOT EXISTS vector;

-- Tạo bảng exercise_embeddings
CREATE TABLE exercise_embeddings (
                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   exercise_id UUID NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
                                   content TEXT NOT NULL,
                                   embedding VECTOR(1536), -- OpenAI text-embedding-3-large dimension
                                   metadata JSONB DEFAULT '{}',
                                   created_at TIMESTAMP DEFAULT NOW(),
                                   updated_at TIMESTAMP DEFAULT NOW(),

                                   CONSTRAINT uq_exercise_embedding UNIQUE (exercise_id)
);

-- Indexes cho performance
CREATE INDEX idx_exercise_embeddings_exercise_id ON exercise_embeddings(exercise_id);
CREATE INDEX idx_exercise_embeddings_metadata ON exercise_embeddings USING GIN (metadata);

-- Vector similarity index (ivfflat với cosine distance)
CREATE INDEX idx_exercise_embeddings_cosine
  ON exercise_embeddings USING ivfflat (embedding vector_cosine_ops)
  WITH (lists = 100);

-- Trigger cho updated_at
CREATE TRIGGER trg_exercise_embeddings_updated_at
  BEFORE UPDATE ON exercise_embeddings
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Analyze để optimize query planner
ANALYZE exercise_embeddings;
