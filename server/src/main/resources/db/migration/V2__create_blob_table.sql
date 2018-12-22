/** blobs: immutable bytes with content type */
CREATE TABLE blobs (
    id           UUID PRIMARY KEY,
    content_type VARCHAR(64) NOT NULL,
    bytes        BYTEA NOT NULL
);
