-- =============================================================
-- CUSTOMER-SERVICE: Cleanup migration
-- Run against: customer_db
-- =============================================================

ALTER TABLE customers DROP COLUMN pharmacy_license;
