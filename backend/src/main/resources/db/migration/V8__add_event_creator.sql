-- Events can be created by users (source = 'USER'); keep track of the creator.
ALTER TABLE events ADD COLUMN created_by VARCHAR(8) REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX idx_events_created_by ON events(created_by);
