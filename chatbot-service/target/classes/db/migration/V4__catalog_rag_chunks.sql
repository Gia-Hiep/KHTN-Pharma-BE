CREATE TABLE IF NOT EXISTS bot_catalog_chunks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_type VARCHAR(30) NOT NULL,
    source_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    metadata_json JSON NULL,
    embedding JSON NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_catalog_chunk_source (source_type, source_id),
    INDEX idx_catalog_chunk_active (active)
);
