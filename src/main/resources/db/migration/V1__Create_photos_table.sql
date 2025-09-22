CREATE TABLE photos (
                        id BIGSERIAL PRIMARY KEY,
                        file_name VARCHAR(255) NOT NULL UNIQUE,
                        original_file_name VARCHAR(255) NOT NULL,
                        description VARCHAR(500) NOT NULL,
                        presigned_url VARCHAR(2048) NOT NULL,
                        file_size BIGINT NOT NULL,
                        content_type VARCHAR(255) NOT NULL,
                        tags VARCHAR(255),
                        location VARCHAR(255),
                        category VARCHAR(255),
                        created_at TIMESTAMP WITHOUT TIME ZONE,
                        updated_at TIMESTAMP WITHOUT TIME ZONE
);

-- Add indexes for performance
CREATE INDEX idx_photo_created_at ON photos (created_at);
CREATE INDEX idx_photo_file_name ON photos (file_name);