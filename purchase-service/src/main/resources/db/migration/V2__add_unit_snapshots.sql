ALTER TABLE `purchase_items`
  ADD COLUMN `unit_code` VARCHAR(30) DEFAULT NULL AFTER `medicine_id`,
  ADD COLUMN `unit_label` VARCHAR(80) DEFAULT NULL AFTER `unit_code`,
  ADD COLUMN `conversion_factor` INT NOT NULL DEFAULT 1 AFTER `unit_label`;

UPDATE `purchase_items`
SET `unit_code` = COALESCE(`unit_code`, 'BASE'),
    `conversion_factor` = CASE WHEN `conversion_factor` IS NULL OR `conversion_factor` < 1 THEN 1 ELSE `conversion_factor` END
WHERE `unit_code` IS NULL OR `conversion_factor` IS NULL OR `conversion_factor` < 1;
