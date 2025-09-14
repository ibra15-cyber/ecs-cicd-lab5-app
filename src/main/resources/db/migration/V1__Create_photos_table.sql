CREATE TABLE photos (
                        id SERIAL PRIMARY KEY,
                        file_name VARCHAR(255) NOT NULL,
                        description TEXT,
                        presigned_url TEXT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_photos_created_at ON photos(created_at);
CREATE INDEX idx_photos_file_name ON photos(file_name);