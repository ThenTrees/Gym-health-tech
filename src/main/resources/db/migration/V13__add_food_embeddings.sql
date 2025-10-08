-- ============================================
-- V13__add_food_embeddings.sql
-- RAG Vector Search
-- ============================================

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE food_embeddings (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               food_id UUID NOT NULL REFERENCES foods(id) ON DELETE CASCADE,
                               content TEXT NOT NULL,
                               embedding VECTOR(1536),
                               metadata JSONB DEFAULT '{}',
                               created_at TIMESTAMP DEFAULT NOW(),
                               updated_at TIMESTAMP DEFAULT NOW(),
                               UNIQUE (food_id)
);

CREATE INDEX idx_food_embeddings_cosine
  ON food_embeddings
  USING ivfflat (embedding vector_cosine_ops)
  WITH (lists = 100);
