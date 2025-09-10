CREATE TABLE IF NOT EXISTS exercise_categories (
                                            code        VARCHAR(32) PRIMARY KEY,
  name        VARCHAR(64) NOT NULL,
  image_url   TEXT
  );

-- Nếu bảng exercises đã tồn tại, chỉ cần thêm cột + FK:
ALTER TABLE exercises
  ADD COLUMN IF NOT EXISTS exercise_category VARCHAR(32);

ALTER TABLE exercises
  ALTER COLUMN exercise_category SET NOT NULL;

ALTER TABLE exercises
  ADD CONSTRAINT fk_exercises_type
    FOREIGN KEY (exercise_category) REFERENCES exercise_categories(code);

CREATE INDEX IF NOT EXISTS idx_exercises_type ON exercises(exercise_category);
-- Insert data into exercise_types
INSERT INTO exercise_categories (code, name, image_url) VALUES
('strength', 'Strength', 'https://cdn.exercisedb.dev/exercisetypes/strength.webp'),
('cardio', 'Cardio', 'https://cdn.exercisedb.dev/exercisetypes/cardio.webp'),
('plyometrics', 'Plyometrics', 'https://cdn.exercisedb.dev/exercisetypes/plyometrics.webp'),
('stretching', 'Stretching', 'https://cdn.exercisedb.dev/exercisetypes/stretching.webp'),
('weightlifting', 'Weightlifting', 'https://cdn.exercisedb.dev/exercisetypes/weightlifting.webp'),
('yoga', 'Yoga', 'https://cdn.exercisedb.dev/exercisetypes/yoga.webp'),
('aerobic', 'Aerobic', 'https://cdn.exercisedb.dev/exercisetypes/aerobic.webp')
ON CONFLICT (code) DO NOTHING;
