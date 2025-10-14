ALTER TABLE nutrition_targets
  ADD COLUMN suggestion_calories_for_breakfast numeric,
  ADD COLUMN suggestion_calories_for_lunch numeric,
  ADD COLUMN suggestion_calories_for_dinner numeric,
  ADD COLUMN is_training boolean DEFAULT false;
