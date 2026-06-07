-- =============================================================
-- REPORTING-SERVICE: Cleanup migration
-- Run against: reporting_db
-- =============================================================

ALTER TABLE audit_logs DROP COLUMN ip_address;
