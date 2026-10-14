CREATE TABLE favorites (
    id VARCHAR(8) PRIMARY KEY,
    client_id VARCHAR(64) NOT NULL,
    event_id VARCHAR(8) NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (client_id, event_id)
);

CREATE INDEX idx_favorites_client_id ON favorites(client_id);
CREATE INDEX idx_favorites_event_id ON favorites(event_id);
