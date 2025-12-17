ALTER TABLE plan_days DROP CONSTRAINT plan_days_day_index_check;
-- hoặc sửa constraint cho day_index >= 0
ALTER TABLE plan_days ADD CONSTRAINT plan_days_day_index_check CHECK (day_index >= 0);
