DELETE FROM favorites;
ALTER TABLE favorites DROP CONSTRAINT IF EXISTS favorites_client_id_event_id_key;
DROP INDEX IF EXISTS idx_favorites_client_id;
ALTER TABLE favorites DROP COLUMN IF EXISTS client_id;
ALTER TABLE favorites ADD COLUMN user_id VARCHAR(8) NOT NULL REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE favorites ADD CONSTRAINT favorites_user_id_event_id_key UNIQUE (user_id, event_id);
CREATE INDEX idx_favorites_user_id ON favorites(user_id);
