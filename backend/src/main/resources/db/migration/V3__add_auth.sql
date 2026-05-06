-- Users table
CREATE TABLE users (
    id            VARCHAR(8) PRIMARY KEY,
    email         VARCHAR(255) UNIQUE NOT NULL,
    username      VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Refresh tokens
CREATE TABLE refresh_tokens (
    id         BIGSERIAL PRIMARY KEY,
    token      VARCHAR(255) UNIQUE NOT NULL,
    user_id    VARCHAR(8) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- Migrate favorites: client_id -> user_id
DELETE FROM favorites;
ALTER TABLE favorites DROP CONSTRAINT favorites_client_id_event_id_key;
DROP INDEX IF EXISTS idx_favorites_client_id;
ALTER TABLE favorites DROP COLUMN client_id;
ALTER TABLE favorites ADD COLUMN user_id VARCHAR(8) NOT NULL REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE favorites ADD CONSTRAINT favorites_user_id_event_id_key UNIQUE (user_id, event_id);
CREATE INDEX idx_favorites_user_id ON favorites(user_id);
