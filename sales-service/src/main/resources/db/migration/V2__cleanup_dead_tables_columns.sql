-- =============================================================
-- SALES-SERVICE: Cleanup migration
-- Removes dead tables and unused columns
-- Run against: sales_db
-- =============================================================

-- Phase 1: Drop dead tables (entities already deleted from Java)
DROP TABLE IF EXISTS packing_slips;
DROP TABLE IF EXISTS shipments;

-- Batch A: Drop unused columns
ALTER TABLE buyer_orders DROP COLUMN payment_qr_code;
ALTER TABLE invoices DROP COLUMN is_debt;
