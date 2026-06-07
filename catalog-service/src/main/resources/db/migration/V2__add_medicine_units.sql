CREATE TABLE IF NOT EXISTS `medicine_units` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `medicine_id` BIGINT NOT NULL,
  `unit_code` VARCHAR(30) NOT NULL,
  `unit_label` VARCHAR(80) NOT NULL,
  `conversion_factor` INT NOT NULL DEFAULT 1,
  `retail_price` DECIMAL(15,2) NOT NULL,
  `wholesale_price` DECIMAL(15,2) DEFAULT NULL,
  `wholesale_min_qty` INT DEFAULT NULL,
  `is_base_unit` TINYINT(1) NOT NULL DEFAULT 0,
  `is_default_sale_unit` TINYINT(1) NOT NULL DEFAULT 0,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_medicine_unit_code` (`medicine_id`, `unit_code`),
  KEY `idx_medicine_units_medicine` (`medicine_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `medicine_units` (
  `medicine_id`,
  `unit_code`,
  `unit_label`,
  `conversion_factor`,
  `retail_price`,
  `wholesale_price`,
  `wholesale_min_qty`,
  `is_base_unit`,
  `is_default_sale_unit`,
  `is_active`
)
SELECT
  m.`id`,
  'BASE',
  m.`unit`,
  1,
  m.`sale_price`,
  NULL,
  NULL,
  1,
  1,
  1
FROM `medicines` m
LEFT JOIN `medicine_units` mu
  ON mu.`medicine_id` = m.`id`
 AND mu.`unit_code` = 'BASE'
WHERE mu.`id` IS NULL;
