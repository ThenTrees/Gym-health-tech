-- V5__add_difficulty_level_to_exercises.sql

-- Thêm cột difficulty_level vào bảng exercises
ALTER TABLE exercises
  ADD COLUMN difficulty_level INTEGER DEFAULT 2
    CHECK (difficulty_level BETWEEN 1 AND 5);

-- Cập nhật difficulty_level dựa trên equipment code thực tế trong database
UPDATE exercises SET difficulty_level =
                       CASE
                         -- BEGINNER (Level 1) - Bodyweight và assisted exercises
                         WHEN equipment IN ('body_weight', 'assisted', 'band', 'resistance_band') THEN 1

                         -- BEGINNER-INTERMEDIATE (Level 2) - Simple equipment, machines
                         WHEN equipment IN (
                                            'dumbbell', 'kettlebell', 'medicine_ball', 'stability_ball', 'bosu_ball',
                                            'stationary_bike', 'elliptical_machine', 'roller', 'wheel_roller'
                           ) THEN 2

                         -- INTERMEDIATE (Level 3) - Standard gym equipment
                         WHEN equipment IN (
                                            'barbell', 'ez_barbell', 'cable', 'rope', 'hammer',
                                            'stepmill_machine', 'skierg_machine', 'upper_body_ergometer'
                           ) THEN 3

                         -- INTERMEDIATE-ADVANCED (Level 4) - Complex equipment, specialized tools
                         WHEN equipment IN (
                                            'olympic_barbell', 'trap_bar', 'smith_machine', 'leverage_machine',
                                            'sled_machine', 'tire', 'weighted'
                           ) THEN 4

                         -- ADVANCED (Level 5) - Specialized/complex equipment (none in current list, reserved for future)
                         -- WHEN equipment IN (...) THEN 5

                         -- Default fallback
                         ELSE 2
                         END;

-- Cập nhật difficulty dựa trên primary muscle complexity (override cho một số muscle groups)
UPDATE exercises SET difficulty_level =
                       CASE
                         -- Core exercises thường dễ hơn với bodyweight
                         WHEN primary_muscle IN ('abdominals', 'abs', 'core', 'obliques')
                           AND equipment = 'body_weight' THEN 1

                         -- Chest và back với barbell thường khó hơn
                         WHEN primary_muscle IN ('pectorals', 'chest', 'latissimus_dorsi', 'lats', 'back')
                           AND equipment IN ('barbell', 'olympic_barbell') THEN 4

                         -- Compound movements với barbell
                         WHEN primary_muscle IN ('quadriceps', 'quads', 'hamstrings', 'glutes')
                           AND equipment IN ('barbell', 'olympic_barbell', 'trap_bar') THEN 4

                         -- Cardio exercises thường intermediate
                         WHEN primary_muscle = 'cardiovascular_system' THEN 2

                         -- Giữ nguyên difficulty đã set ở trên
                         ELSE difficulty_level
                         END;

-- Tạo index cho performance
CREATE INDEX idx_exercises_difficulty ON exercises(difficulty_level);

-- Tạo index composite cho filtering thường dùng
CREATE INDEX idx_exercises_equipment_difficulty ON exercises(equipment, difficulty_level);
CREATE INDEX idx_exercises_muscle_difficulty ON exercises(primary_muscle, difficulty_level);

-- Verify kết quả
-- Uncomment để kiểm tra distribution
/*
SELECT
    difficulty_level,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 2) as percentage
FROM exercises
WHERE is_deleted = false
GROUP BY difficulty_level
ORDER BY difficulty_level;

-- Kiểm tra difficulty theo equipment
SELECT
    eq.name as equipment_name,
    e.difficulty_level,
    COUNT(*) as count
FROM exercises e
JOIN equipments eq ON e.equipment = eq.code
WHERE e.is_deleted = false
GROUP BY eq.name, e.difficulty_level
ORDER BY eq.name, e.difficulty_level;
*/
