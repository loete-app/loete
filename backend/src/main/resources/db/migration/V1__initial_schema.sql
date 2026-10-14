CREATE TABLE sample_items (
    id          VARCHAR(8) PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    category    VARCHAR(50) NOT NULL,
    priority    VARCHAR(50) NOT NULL,
    price       DOUBLE PRECISION NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(255),
    country VARCHAR(100) DEFAULT 'CH',
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION
);

INSERT INTO categories (name, slug) VALUES
    ('Konzert', 'konzert'),
    ('Sport', 'sport'),
    ('Theater', 'theater'),
    ('Festival', 'festival'),
    ('Comedy', 'comedy'),
    ('Messe', 'messe'),
    ('Sonstiges', 'sonstiges');

CREATE TABLE events (
    id VARCHAR(8) PRIMARY KEY,
    external_id VARCHAR(255) UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(1024),
    ticket_url VARCHAR(1024),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    category_id BIGINT REFERENCES categories(id),
    location_id BIGINT REFERENCES locations(id),
    source VARCHAR(50) DEFAULT 'TICKETMASTER',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_events_external_id ON events(external_id);
CREATE INDEX idx_events_start_date ON events(start_date);
CREATE INDEX idx_events_category_id ON events(category_id);
