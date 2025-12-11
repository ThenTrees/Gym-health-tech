ALTER TABLE plan_items DROP CONSTRAINT plan_items_item_index_check;
-- hoặc sửa constraint cho day_index >= 0
ALTER TABLE plan_items ADD CONSTRAINT plan_items_item_index_check CHECK (item_index >= 0);
