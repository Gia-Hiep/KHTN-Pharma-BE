-- phpMyAdmin SQL Dump
-- version 5.0.2
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1:3306
-- Thời gian đã tạo: Th3 02, 2026 lúc 01:19 PM
-- Phiên bản máy phục vụ: 5.7.31
-- Phiên bản PHP: 7.3.21

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `sales_db`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `buyer_orders`
--

DROP TABLE IF EXISTS `buyer_orders`;
CREATE TABLE IF NOT EXISTS `buyer_orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `buyer_id` bigint(20) NOT NULL,
  `coupon_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `discount` decimal(38,2) DEFAULT NULL,
  `notes` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `payment_method` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `processed_by` bigint(20) DEFAULT NULL,
  `rejection_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shipping_address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('CANCELLED','CONFIRMED','DELIVERED','PACKING','PENDING_APPROVAL','PICKING','REJECTED','SHIPPING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `subtotal` decimal(38,2) DEFAULT NULL,
  `total` decimal(38,2) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `carrier` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `delivered_at` datetime(6) DEFAULT NULL,
  `requires_prescription` bit(1) DEFAULT NULL,
  `shipped_at` datetime(6) DEFAULT NULL,
  `tracking_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `buyer_orders`
--

INSERT INTO `buyer_orders` (`id`, `buyer_id`, `coupon_code`, `created_at`, `discount`, `notes`, `payment_method`, `processed_by`, `rejection_reason`, `shipping_address`, `status`, `subtotal`, `total`, `updated_at`, `carrier`, `delivered_at`, `requires_prescription`, `shipped_at`, `tracking_code`) VALUES
(1, 6, NULL, '2026-02-26 22:02:12.966201', '0.00', 'dsasd', 'CASH', 4, NULL, 'adsdsa', 'DELIVERED', '8000.00', '8000.00', '2026-02-26 22:02:33.086825', NULL, NULL, NULL, NULL, NULL),
(2, 6, NULL, '2026-02-28 15:15:52.323450', '0.00', '', 'CASH', 4, NULL, 'dsasa', 'PACKING', '8000.00', '8000.00', '2026-02-28 15:17:24.181394', NULL, NULL, NULL, NULL, NULL),
(3, 6, NULL, '2026-03-01 19:54:53.624473', '0.00', 'dsa', 'CASH', NULL, NULL, 'sdasdasadas', 'PENDING_APPROVAL', '1800.00', '1800.00', NULL, NULL, NULL, b'0', NULL, NULL),
(4, 6, 'dá', '2026-03-01 20:00:10.589119', '0.00', 'dsads', 'CASH', 4, NULL, 'đasa', 'CONFIRMED', '404404.00', '404404.00', '2026-03-01 20:00:38.229630', NULL, NULL, b'0', NULL, NULL),
(5, 6, 'dsasda', '2026-03-01 20:29:46.349897', '0.00', 'adsads', 'CASH', 4, NULL, 'dsasda', 'DELIVERED', '224000.00', '224000.00', '2026-03-01 20:30:23.263805', 'GIAO_HANG_NHANH', '2026-03-01 20:30:23.258589', b'1', '2026-03-01 20:30:19.235441', 'GHN1123'),
(6, 6, 'dsadsa', '2026-03-01 20:31:18.874209', '0.00', 'sad', 'CASH', 4, 'ko đủ hàng', 'dsa', 'REJECTED', '248864.00', '248864.00', '2026-03-01 20:31:54.109054', NULL, NULL, b'0', NULL, NULL),
(7, 6, 'dsa', '2026-03-01 20:32:44.640282', '0.00', 'ád', 'CASH', 4, NULL, 'dsa', 'DELIVERED', '222000.00', '222000.00', '2026-03-01 21:01:05.824576', 'GIAO_HANG_NHANH', '2026-03-01 21:01:05.821548', b'1', '2026-03-01 21:01:04.171619', 'jhkh');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `buyer_order_items`
--

DROP TABLE IF EXISTS `buyer_order_items`;
CREATE TABLE IF NOT EXISTS `buyer_order_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `line_total` decimal(38,2) DEFAULT NULL,
  `medicine_id` bigint(20) NOT NULL,
  `medicine_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `price_tier` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `qty` int(11) NOT NULL,
  `unit_price` decimal(38,2) DEFAULT NULL,
  `order_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3j6cm5mxk3fhs4crd3ge8ldpy` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `buyer_order_items`
--

INSERT INTO `buyer_order_items` (`id`, `line_total`, `medicine_id`, `medicine_name`, `price_tier`, `qty`, `unit_price`, `order_id`) VALUES
(1, '8000.00', 1, 'amoxicillin 501mg', 'RETAIL', 4, '2000.00', 1),
(2, '8000.00', 1, 'amoxicillin 501mg', 'RETAIL', 4, '2000.00', 2),
(3, '1800.00', 1, 'amoxicillin 501mg', 'WHOLESALE', 1, '1800.00', 3),
(4, '404404.00', 4, '12', 'RETAIL', 182, '2222.00', 4),
(5, '224000.00', 2, 'paracetamol 500mg', 'RETAIL', 112, '2000.00', 5),
(6, '248864.00', 4, '12', 'RETAIL', 112, '2222.00', 6),
(7, '222000.00', 2, 'paracetamol 500mg', 'RETAIL', 111, '2000.00', 7);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `daily_sales_summary`
--

DROP TABLE IF EXISTS `daily_sales_summary`;
CREATE TABLE IF NOT EXISTS `daily_sales_summary` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `report_date` date NOT NULL,
  `total_invoices` int(11) NOT NULL DEFAULT '0',
  `paid_invoices` int(11) NOT NULL DEFAULT '0',
  `cancelled_invoices` int(11) NOT NULL DEFAULT '0',
  `total_revenue` decimal(18,2) NOT NULL DEFAULT '0.00',
  `total_discount` decimal(18,2) NOT NULL DEFAULT '0.00',
  `net_revenue` decimal(18,2) NOT NULL DEFAULT '0.00',
  `retail_revenue` decimal(18,2) DEFAULT '0.00',
  `wholesale_revenue` decimal(18,2) DEFAULT '0.00',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_summary_date` (`report_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `invoices`
--

DROP TABLE IF EXISTS `invoices`;
CREATE TABLE IF NOT EXISTS `invoices` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `customer_id` bigint(20) DEFAULT NULL,
  `prescription_id` bigint(20) DEFAULT NULL,
  `cashier_id` bigint(20) NOT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `payment_status` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `subtotal` decimal(38,2) NOT NULL,
  `discount` decimal(38,2) NOT NULL,
  `total` decimal(38,2) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `notes` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `order_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'RETAIL' COMMENT 'RETAIL, WHOLESALE, ONLINE',
  `channel` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'POS' COMMENT 'POS, WEB, APP',
  `requires_shipping` tinyint(1) NOT NULL DEFAULT '0',
  `shipping_address` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shipping_fee` decimal(15,2) NOT NULL DEFAULT '0.00',
  `shipping_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'PENDING, PICKED_UP, IN_TRANSIT, DELIVERED',
  `tracking_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contains_rx` tinyint(1) NOT NULL DEFAULT '0',
  `rx_approval_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'PENDING, APPROVED, REJECTED',
  `rx_approved_by` bigint(20) DEFAULT NULL,
  `rx_approved_at` datetime DEFAULT NULL,
  `rx_rejection_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `coupon_code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `coupon_discount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `is_debt` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'For B2B payment later',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_inv_created` (`created_at`),
  KEY `idx_inv_cashier` (`cashier_id`),
  KEY `fk_inv_customer` (`customer_id`),
  KEY `idx_inv_order_type` (`order_type`),
  KEY `idx_inv_channel` (`channel`),
  KEY `idx_inv_rx_status` (`rx_approval_status`),
  KEY `idx_inv_shipping` (`requires_shipping`,`shipping_status`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `invoices`
--

INSERT INTO `invoices` (`id`, `code`, `customer_id`, `prescription_id`, `cashier_id`, `status`, `payment_status`, `subtotal`, `discount`, `total`, `created_at`, `updated_at`, `notes`, `order_type`, `channel`, `requires_shipping`, `shipping_address`, `shipping_fee`, `shipping_status`, `tracking_code`, `contains_rx`, `rx_approval_status`, `rx_approved_by`, `rx_approved_at`, `rx_rejection_reason`, `coupon_code`, `coupon_discount`, `is_debt`) VALUES
(8, 'INV_DEMO_001', 4, NULL, 1, 'PAID', 'PAID', '2000.00', '0.00', '2000.00', '2026-01-10 07:16:48', '2026-01-10 07:18:41', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, '0.00', 0),
(9, 'inv_1766627303320', 4, 4, 1, 'PAID', 'PAID', '16910.00', '0.00', '16910.00', '2026-01-10 20:11:19', '2026-01-10 20:11:19', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, '0.00', 0),
(10, 'inv_1766628689091', NULL, NULL, 1, 'DRAFT', 'UNPAID', '0.00', '0.00', '0.00', '2026-01-10 20:11:19', '2026-01-10 20:11:19', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, '0.00', 0),
(11, 'inv_1766632913511', 2, 5, 1, 'DRAFT', 'UNPAID', '0.00', '0.00', '0.00', '2025-12-25 10:22:09', '2026-01-10 20:11:19', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, '0.00', 0),
(12, 'INV-20260109-232720', NULL, NULL, 3, 'DRAFT', 'UNPAID', '2000.00', '0.00', '2000.00', '2026-01-10 20:11:19', '2026-01-10 20:11:19', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, '0.00', 0),
(13, 'INV-20260109-233112', NULL, NULL, 3, 'DRAFT', 'UNPAID', '0.00', '0.00', '0.00', '2026-01-10 20:11:19', '2026-01-10 20:11:19', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, '0.00', 0),
(14, 'inv_1767976588677', NULL, NULL, 3, 'CANCELLED', 'UNPAID', '22000.00', '0.00', '22000.00', '2026-01-10 20:11:19', '2026-01-10 20:11:19', 'thich', 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, '0.00', 0),
(15, 'INV-20260109-234215', NULL, NULL, 3, 'DRAFT', 'UNPAID', '0.00', '0.00', '0.00', '2026-01-10 20:11:19', '2026-01-10 20:11:19', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, '0.00', 0),
(16, 'INV-20260109-235218', NULL, NULL, 3, 'DRAFT', 'UNPAID', '0.00', '0.00', '0.00', '2026-01-10 20:11:19', '2026-01-10 07:20:48', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, '0.00', 0),
(17, 'INV-20260226-503BE', 7, NULL, 4, 'DRAFT', 'UNPAID', '13110.00', '0.00', '13110.00', '2026-02-26 20:01:29', '2026-02-26 20:07:11', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, '0.00', 0),
(18, 'INV-20260226-F4A07', 7, NULL, 4, 'DRAFT', 'UNPAID', '9000.00', '0.00', '9000.00', '2026-02-26 20:21:21', '2026-02-26 20:21:32', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, '0.00', 0),
(19, 'INV-20260226-EE906', 7, NULL, 4, 'WAIT_PAYMENT', 'UNPAID', '3800.00', '0.00', '3800.00', '2026-02-26 20:27:08', '2026-02-26 20:28:42', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, '0.00', 0),
(20, 'INV-20260226-C848B', 7, NULL, 4, 'WAIT_PAYMENT', 'UNPAID', '2000.00', '0.00', '2000.00', '2026-02-26 20:31:21', '2026-02-26 20:31:29', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, '0.00', 0);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `invoice_items`
--

DROP TABLE IF EXISTS `invoice_items`;
CREATE TABLE IF NOT EXISTS `invoice_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `invoice_id` bigint(20) NOT NULL,
  `medicine_id` bigint(20) NOT NULL,
  `qty` int(11) NOT NULL,
  `unit_price` decimal(38,2) NOT NULL,
  `line_total` decimal(38,2) NOT NULL,
  `medicine_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_item_inv` (`invoice_id`),
  KEY `idx_item_med` (`medicine_id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `invoice_items`
--

INSERT INTO `invoice_items` (`id`, `invoice_id`, `medicine_id`, `qty`, `unit_price`, `line_total`, `medicine_name`) VALUES
(8, 8, 1, 2, '1000.00', '2000.00', 'amoxicillin 501mg'),
(9, 9, 1, 1, '2000.00', '2000.00', 'amoxicillin 501mg'),
(10, 9, 2, 1, '2000.00', '2000.00', 'paracetamol 500mg'),
(11, 9, 3, 1, '1800.00', '1800.00', 'paracetamol 500 test updated'),
(12, 9, 4, 1, '2222.00', '2222.00', '12'),
(13, 9, 4, 4, '2222.00', '8888.00', '12'),
(14, 12, 1, 1, '2000.00', '2000.00', 'amoxicillin 501mg'),
(15, 14, 1, 10, '2000.00', '20000.00', 'amoxicillin 501mg'),
(16, 14, 2, 1, '2000.00', '2000.00', 'paracetamol 500mg'),
(17, 17, 1, 1, '2000.00', '2000.00', 'amoxicillin 501mg'),
(18, 17, 4, 5, '2222.00', '11110.00', '12'),
(19, 18, 3, 5, '1800.00', '9000.00', 'paracetamol 500 test updated'),
(20, 19, 3, 1, '1800.00', '1800.00', 'paracetamol 500 test updated'),
(21, 19, 2, 1, '2000.00', '2000.00', 'paracetamol 500mg'),
(22, 20, 2, 1, '2000.00', '2000.00', 'paracetamol 500mg');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `invoice_lot_allocations`
--

DROP TABLE IF EXISTS `invoice_lot_allocations`;
CREATE TABLE IF NOT EXISTS `invoice_lot_allocations` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `invoice_item_id` bigint(20) NOT NULL,
  `lot_id` bigint(20) NOT NULL,
  `qty` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_alloc_item` (`invoice_item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `invoice_lot_allocations`
--

INSERT INTO `invoice_lot_allocations` (`id`, `invoice_item_id`, `lot_id`, `qty`) VALUES
(7, 8, 39, 2),
(8, 9, 39, 1),
(9, 10, 42, 1),
(10, 11, 44, 1),
(11, 12, 45, 1),
(12, 12, 45, 4),
(13, 20, 44, 1),
(14, 21, 42, 1),
(15, 22, 42, 1);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `packing_slips`
--

DROP TABLE IF EXISTS `packing_slips`;
CREATE TABLE IF NOT EXISTS `packing_slips` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `invoice_id` bigint(20) NOT NULL,
  `printed_by` bigint(20) NOT NULL,
  `printed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `slip_data` json DEFAULT NULL COMMENT 'Snapshot of invoice data for printing',
  PRIMARY KEY (`id`),
  KEY `idx_slip_inv` (`invoice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `payments`
--

DROP TABLE IF EXISTS `payments`;
CREATE TABLE IF NOT EXISTS `payments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `invoice_id` bigint(20) NOT NULL,
  `amount` decimal(38,2) NOT NULL,
  `payment_method` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `transaction_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `paid_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `meta` json DEFAULT NULL,
  `notes` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pay_inv` (`invoice_id`),
  KEY `idx_pay_txid` (`transaction_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `payments`
--

INSERT INTO `payments` (`id`, `invoice_id`, `amount`, `payment_method`, `transaction_id`, `status`, `paid_at`, `meta`, `notes`) VALUES
(3, 8, '2000.00', 'CASH', 'TX_DEMO_001', 'SUCCESS', '2025-12-23 20:11:29', NULL, NULL),
(4, 9, '16910.00', 'CASH', NULL, 'SUCCESS', '2025-12-25 09:08:56', NULL, NULL);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `shipments`
--

DROP TABLE IF EXISTS `shipments`;
CREATE TABLE IF NOT EXISTS `shipments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `invoice_id` bigint(20) NOT NULL,
  `carrier` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'GHN, GHTK, Viettel Post, etc.',
  `tracking_number` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, PICKED_UP, IN_TRANSIT, DELIVERED, FAILED',
  `shipping_fee` decimal(15,2) NOT NULL,
  `estimated_delivery` date DEFAULT NULL,
  `actual_delivery` datetime DEFAULT NULL,
  `receiver_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `receiver_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `receiver_address` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `notes` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_ship_inv` (`invoice_id`),
  KEY `idx_ship_tracking` (`tracking_number`),
  KEY `idx_ship_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `buyer_order_items`
--
ALTER TABLE `buyer_order_items`
  ADD CONSTRAINT `FK3j6cm5mxk3fhs4crd3ge8ldpy` FOREIGN KEY (`order_id`) REFERENCES `buyer_orders` (`id`);

--
-- Các ràng buộc cho bảng `invoice_items`
--
ALTER TABLE `invoice_items`
  ADD CONSTRAINT `fk_item_inv` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `invoice_lot_allocations`
--
ALTER TABLE `invoice_lot_allocations`
  ADD CONSTRAINT `fk_alloc_item` FOREIGN KEY (`invoice_item_id`) REFERENCES `invoice_items` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `packing_slips`
--
ALTER TABLE `packing_slips`
  ADD CONSTRAINT `fk_slip_inv` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `payments`
--
ALTER TABLE `payments`
  ADD CONSTRAINT `fk_pay_inv` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `shipments`
--
ALTER TABLE `shipments`
  ADD CONSTRAINT `fk_ship_inv` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
