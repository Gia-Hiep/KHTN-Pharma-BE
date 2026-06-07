ALTER TABLE `cart_items`
  ADD COLUMN `unit_code` VARCHAR(30) DEFAULT NULL AFTER `price_tier`,
  ADD COLUMN `unit_label` VARCHAR(80) DEFAULT NULL AFTER `unit_code`,
  ADD COLUMN `conversion_factor` INT NOT NULL DEFAULT 1 AFTER `unit_label`;

ALTER TABLE `buyer_order_items`
  ADD COLUMN `unit_code` VARCHAR(30) DEFAULT NULL AFTER `price_tier`,
  ADD COLUMN `unit_label` VARCHAR(80) DEFAULT NULL AFTER `unit_code`,
  ADD COLUMN `conversion_factor` INT NOT NULL DEFAULT 1 AFTER `unit_label`;

ALTER TABLE `invoice_items`
  ADD COLUMN `unit_code` VARCHAR(30) DEFAULT NULL AFTER `medicine_name`,
  ADD COLUMN `unit_label` VARCHAR(80) DEFAULT NULL AFTER `unit_code`,
  ADD COLUMN `conversion_factor` INT NOT NULL DEFAULT 1 AFTER `unit_label`,
  ADD COLUMN `sale_mode` VARCHAR(20) NOT NULL DEFAULT 'RETAIL' AFTER `conversion_factor`;

UPDATE `cart_items`
SET `unit_code` = COALESCE(`unit_code`, 'BASE'),
    `conversion_factor` = CASE WHEN `conversion_factor` IS NULL OR `conversion_factor` < 1 THEN 1 ELSE `conversion_factor` END
WHERE `unit_code` IS NULL OR `conversion_factor` IS NULL OR `conversion_factor` < 1;

UPDATE `buyer_order_items`
SET `unit_code` = COALESCE(`unit_code`, 'BASE'),
    `conversion_factor` = CASE WHEN `conversion_factor` IS NULL OR `conversion_factor` < 1 THEN 1 ELSE `conversion_factor` END
WHERE `unit_code` IS NULL OR `conversion_factor` IS NULL OR `conversion_factor` < 1;

UPDATE `invoice_items` ii
LEFT JOIN `invoices` i ON i.`id` = ii.`invoice_id`
SET ii.`unit_code` = COALESCE(ii.`unit_code`, 'BASE'),
    ii.`conversion_factor` = CASE
        WHEN ii.`conversion_factor` IS NULL OR ii.`conversion_factor` < 1 THEN 1
        ELSE ii.`conversion_factor`
    END,
    ii.`sale_mode` = CASE
        WHEN i.`order_type` = 'WHOLESALE' THEN 'WHOLESALE'
        ELSE 'RETAIL'
    END
WHERE ii.`unit_code` IS NULL
   OR ii.`conversion_factor` IS NULL
   OR ii.`conversion_factor` < 1
   OR ii.`sale_mode` IS NULL
   OR ii.`sale_mode` = '';

ALTER TABLE `cart_items`
  DROP INDEX `UK65234xcuasjjm0rj2vkwr9f4i`,
  ADD UNIQUE KEY `uk_cart_med_sale_mode_unit` (`cart_id`, `medicine_id`, `price_tier`, `unit_code`);
