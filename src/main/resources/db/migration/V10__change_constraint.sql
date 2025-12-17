-- 1️⃣ Xóa constraint cũ
ALTER TABLE nutrition_targets
DROP CONSTRAINT nutrition_targets_user_id_goal_id_key;

-- 2️⃣ Tạo constraint mới (thêm is_training)
ALTER TABLE nutrition_targets
  ADD CONSTRAINT nutrition_targets_user_id_goal_id_is_training_key
    UNIQUE (user_id, goal_id, is_training);
