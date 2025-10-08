-- DROP TABLE IF EXISTS nutrition_targets;
DROP TABLE IF EXISTS meal_plans;


-- 2. Bảng FOODS
CREATE TABLE foods (
                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                     food_name VARCHAR(255) NOT NULL,
                     food_name_vi VARCHAR(255),
                     description TEXT,

  -- Nutrition per serving
                     serving_weight_grams NUMERIC(7,2) NOT NULL,
                     calories NUMERIC(7,2) NOT NULL,
                     protein NUMERIC(6,2) NOT NULL DEFAULT 0,
                     carbs NUMERIC(6,2) NOT NULL DEFAULT 0,
                     fat NUMERIC(6,2) NOT NULL DEFAULT 0,
                     fiber NUMERIC(6,2) DEFAULT 0,

  -- Vitamins
                     vitamin_a NUMERIC(8,2),
                     vitamin_c NUMERIC(8,2),
                     vitamin_d NUMERIC(8,2),

  -- Classification
                     category VARCHAR(50) NOT NULL,
                     meal_time VARCHAR(100),
                     image_url TEXT,
                     detailed_benefits TEXT,
                     preparation_tips TEXT,
                     common_combinations TEXT,
                     contraindications TEXT,

  -- Metadata
                     tags TEXT[],
                     is_active BOOLEAN NOT NULL DEFAULT true,

  -- Audit
                     created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                     updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                     version INT NOT NULL DEFAULT 0,
                     is_deleted BOOLEAN NOT NULL DEFAULT false,
                     created_by UUID,
                     updated_by UUID
);

-- 3. Bảng MEAL_TIMES
CREATE TABLE meal_times (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          code VARCHAR(32) NOT NULL UNIQUE,
                          name VARCHAR(50) NOT NULL,
                          name_vi VARCHAR(50) NOT NULL,
                          display_order INT NOT NULL,
                          icon VARCHAR(50),
                          default_calorie_percentage NUMERIC(5,2) NOT NULL DEFAULT 25.00,
                          created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO meal_times (code, name, name_vi, display_order, icon, default_calorie_percentage) VALUES
                                                                                                ('breakfast', 'Breakfast', 'Sáng', 1, '🌅', 25.00),
                                                                                                ('snack_morning', 'Morning Snack', 'Phụ Sáng', 2, '☕', 10.00),
                                                                                                ('lunch', 'Lunch', 'Trưa', 3, '🍽️', 35.00),
                                                                                                ('snack_afternoon', 'Afternoon Snack', 'Phụ Chiều', 4, '🥤', 10.00),
                                                                                                ('dinner', 'Dinner', 'Tối', 5, '🌙', 25.00),
                                                                                                ('snack_evening', 'Evening Snack', 'Phụ Tối', 6, '🍪', 5.00);

-- 4. Enhance NUTRITION_TARGETS
ALTER TABLE nutrition_targets
  ADD COLUMN bmr INT,
    ADD COLUMN tdee INT,
    ADD COLUMN activity_level VARCHAR(32),
    ADD COLUMN goal_type VARCHAR(32),
    ADD COLUMN is_active BOOLEAN DEFAULT true,
    ADD COLUMN updated_at TIMESTAMP DEFAULT NOW();

CREATE UNIQUE INDEX uq_nutrition_target_active
  ON nutrition_targets(user_id) WHERE is_active = true;

-- 5. MEAL_PLANS
CREATE TABLE meal_plans (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                          plan_date DATE NOT NULL,

                          total_calories INT,
                          total_protein NUMERIC(6,2),
                          total_carbs NUMERIC(6,2),
                          total_fat NUMERIC(6,2),

                          is_training_day BOOLEAN DEFAULT false,
                          base_calories INT,
                          workout_adjustment INT DEFAULT 0,

                          ai_reasoning TEXT,
                          ai_tips JSONB DEFAULT '[]',

                          status VARCHAR(32) DEFAULT 'generated',
                          created_at TIMESTAMP DEFAULT NOW(),
                          updated_at TIMESTAMP DEFAULT NOW(),

                          UNIQUE (user_id, plan_date)
);

CREATE INDEX idx_meal_plans_user_date ON meal_plans(user_id, plan_date);

-- 6. MEAL_PLAN_ITEMS
CREATE TABLE meal_plan_items (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               meal_plan_id UUID NOT NULL REFERENCES meal_plans(id) ON DELETE CASCADE,
                               meal_time_id UUID NOT NULL REFERENCES meal_times(id),
                               food_id UUID REFERENCES foods(id),

                               food_name VARCHAR(255) NOT NULL,
                               servings NUMERIC(6,2) NOT NULL DEFAULT 1,
                               calories NUMERIC(7,2) NOT NULL,
                               protein NUMERIC(6,2) NOT NULL,
                               carbs NUMERIC(6,2) NOT NULL,
                               fat NUMERIC(6,2) NOT NULL,

                               is_completed BOOLEAN DEFAULT false,
                               completed_at TIMESTAMP,
                               display_order INT DEFAULT 0,

                               created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_meal_plan_items_plan ON meal_plan_items(meal_plan_id);
