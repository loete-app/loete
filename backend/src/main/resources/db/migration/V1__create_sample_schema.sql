CREATE TABLE sample_items (
    id          VARCHAR(8) PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    category    VARCHAR(50) NOT NULL,
    priority    VARCHAR(50) NOT NULL,
    price       DOUBLE PRECISION NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
