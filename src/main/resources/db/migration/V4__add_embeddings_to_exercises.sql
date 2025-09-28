-- V4__add_embedding_vector_to_exercises.sql
-- Thêm cột embedding (pgvector) cho bảng exercises + index ivfflat (cosine)

-- 1) Bảo đảm extension pgvector đã có
CREATE EXTENSION IF NOT EXISTS vector;

-- 2) Thêm cột embedding 1536 chiều (phù hợp OpenAI text-embedding-3-large/small)
ALTER TABLE exercises
  ADD COLUMN IF NOT EXISTS embedding VECTOR(1536);

-- 3) (Tuỳ chọn) Nếu đã có index cũ, drop để tạo lại với tham số lists mong muốn
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM pg_indexes
    WHERE schemaname = 'public' AND indexname = 'idx_exercises_embedding'
  ) THEN
    EXECUTE 'DROP INDEX IF EXISTS idx_exercises_embedding';
END IF;
END $$;

-- 4) Tạo ANN index ivfflat với cosine (điều chỉnh lists theo dữ liệu của bạn)
--    Lưu ý: ivfflat hiệu quả hơn khi bảng có ~1000+ dòng.
CREATE INDEX idx_exercises_embedding
  ON exercises
  USING ivfflat (embedding vector_cosine_ops)
  WITH (lists = 100);

-- 5) Khuyến nghị phân tích lại để planner tối ưu
ANALYZE exercises;
