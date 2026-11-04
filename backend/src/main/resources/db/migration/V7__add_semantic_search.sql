CREATE EXTENSION IF NOT EXISTS vector;

ALTER TABLE events
    ADD COLUMN embedding vector(1536),
    ADD COLUMN embedding_input TEXT,
    ADD COLUMN embedded_at TIMESTAMP;

CREATE INDEX idx_events_embedding_hnsw
    ON events USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);
