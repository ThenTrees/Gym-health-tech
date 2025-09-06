-- V1_2__add_object_key_to_user_profile.sql
ALTER TABLE user_profiles
  ADD COLUMN object_key VARCHAR(255) DEFAULT NULL;
