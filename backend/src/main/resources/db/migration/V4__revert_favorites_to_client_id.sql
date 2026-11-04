-- Revert favorites back to client_id (supports both anonymous and authenticated use)
DELETE FROM favorites;
ALTER TABLE favorites DROP CONSTRAINT IF EXISTS favorites_user_id_event_id_key;
DROP INDEX IF EXISTS idx_favorites_user_id;
ALTER TABLE favorites DROP COLUMN IF EXISTS user_id;
ALTER TABLE favorites ADD COLUMN IF NOT EXISTS client_id VARCHAR(64) NOT NULL;
ALTER TABLE favorites ADD CONSTRAINT favorites_client_id_event_id_key UNIQUE (client_id, event_id);
CREATE INDEX IF NOT EXISTS idx_favorites_client_id ON favorites(client_id);
