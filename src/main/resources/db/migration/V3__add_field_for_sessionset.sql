ALTER TABLE session_sets
  ADD COLUMN plan_item_id uuid NULL REFERENCES plan_items(id) ON DELETE SET NULL;

-- (tuỳ chọn) index để truy nhanh theo plan_item
CREATE INDEX idx_session_sets_plan_item ON session_sets(plan_item_id);
