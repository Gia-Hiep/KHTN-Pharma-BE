SET @faq_id_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'bot_document_chunks'
      AND column_name = 'faq_id'
);
SET @sql = IF(
    @faq_id_exists = 0,
    'ALTER TABLE bot_document_chunks ADD COLUMN faq_id BIGINT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @source_type_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'bot_document_chunks'
      AND column_name = 'source_type'
);
SET @sql = IF(
    @source_type_exists = 0,
    'ALTER TABLE bot_document_chunks ADD COLUMN source_type VARCHAR(30) NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @source_title_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'bot_document_chunks'
      AND column_name = 'source_title'
);
SET @sql = IF(
    @source_title_exists = 0,
    'ALTER TABLE bot_document_chunks ADD COLUMN source_title VARCHAR(255) NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_chunk_faq_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'bot_document_chunks'
      AND index_name = 'idx_chunk_faq'
);
SET @sql = IF(
    @idx_chunk_faq_exists = 0,
    'CREATE INDEX idx_chunk_faq ON bot_document_chunks (faq_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE bot_faqs
SET active = 0
WHERE active = 1
  AND (
    UPPER(intent) IN ('DOSAGE', 'PRESCRIPTION', 'SIDE_EFFECTS')
    OR LOWER(question) REGEXP 'lieu dung|uong bao nhieu|may vien|ke don|chan doan|tre em|ba bau|phu nu co thai|benh nen'
    OR LOWER(keywords) REGEXP 'lieu dung|uong bao nhieu|may vien|ke don|chan doan|tre em|ba bau|phu nu co thai|benh nen'
  );
