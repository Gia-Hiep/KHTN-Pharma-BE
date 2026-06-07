-- phpMyAdmin SQL Dump
-- version 5.0.2
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1:3306
-- Thời gian đã tạo: Th5 04, 2026 lúc 01:45 PM
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
-- Cơ sở dữ liệu: `reporting_db`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `audit_logs`
--

DROP TABLE IF EXISTS `audit_logs`;
CREATE TABLE IF NOT EXISTS `audit_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `service_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `action` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'CREATE, UPDATE, DELETE, LOGIN, LOGOUT, VIEW',
  `entity_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `entity_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `user_email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `detail` text COLLATE utf8mb4_unicode_ci COMMENT 'JSON payload of changes',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_service` (`service_name`),
  KEY `idx_action` (`action`),
  KEY `idx_user` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `audit_logs`
--

INSERT INTO `audit_logs` (`id`, `service_name`, `action`, `entity_type`, `entity_id`, `user_id`, `user_email`, `detail`, `created_at`) VALUES
(1, 'sales-service', 'CREATE', 'Invoice', 1001, 2, 'staff@pharmacy.com', '{\"total\":250000,\"items\":3}', '2026-02-26 10:02:58'),
(2, 'sales-service', 'UPDATE', 'Invoice', 1001, 2, 'staff@pharmacy.com', '{\"status\":\"PAID\"}', '2026-02-26 10:02:58'),
(3, 'catalog-service', 'CREATE', 'Medicine', 15, 1, 'admin@pharmacy.com', '{\"name\":\"Metformin 1000mg\"}', '2026-02-26 10:02:58'),
(4, 'catalog-service', 'UPDATE', 'Medicine', 3, 1, 'admin@pharmacy.com', '{\"price\":{\"old\":15000,\"new\":16000}}', '2026-02-26 10:02:58'),
(5, 'auth-service', 'LOGIN', NULL, NULL, 2, 'staff@pharmacy.com', '{\"success\":true}', '2026-02-26 10:02:58'),
(6, 'auth-service', 'LOGIN', NULL, NULL, 1, 'admin@pharmacy.com', '{\"success\":true}', '2026-02-26 10:02:58'),
(7, 'customer-service', 'CREATE', 'Customer', 301, 2, 'staff@pharmacy.com', '{\"name\":\"Nguyen Van A\"}', '2026-02-26 10:02:58');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `customer_region_stats`
--

DROP TABLE IF EXISTS `customer_region_stats`;
CREATE TABLE IF NOT EXISTS `customer_region_stats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `report_month` date NOT NULL COMMENT 'Ngày đầu tháng',
  `region` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `customer_count` int(11) NOT NULL DEFAULT '0',
  `new_customers` int(11) NOT NULL DEFAULT '0',
  `total_orders` int(11) NOT NULL DEFAULT '0',
  `total_revenue` decimal(18,2) NOT NULL DEFAULT '0.00',
  `avg_order_value` decimal(18,2) NOT NULL DEFAULT '0.00',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_region_month` (`report_month`,`region`),
  UNIQUE KEY `UKg192ba12s5x78radlikr2enrk` (`report_month`,`region`),
  KEY `idx_report_month` (`report_month`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `customer_region_stats`
--

INSERT INTO `customer_region_stats` (`id`, `report_month`, `region`, `customer_count`, `new_customers`, `total_orders`, `total_revenue`, `avg_order_value`, `created_at`) VALUES
(1, '2026-02-01', 'TP. Hồ Chí Minh', 450, 32, 1850, '85000000.00', '45945.00', '2026-02-26 10:02:58'),
(2, '2026-02-01', 'Hà Nội', 280, 18, 950, '42000000.00', '44210.00', '2026-02-26 10:02:58'),
(3, '2026-02-01', 'Đà Nẵng', 120, 10, 380, '16500000.00', '43421.00', '2026-02-26 10:02:58'),
(4, '2026-02-01', 'Cần Thơ', 90, 7, 270, '11200000.00', '41481.00', '2026-02-26 10:02:58'),
(5, '2026-02-01', 'Bình Dương', 85, 5, 240, '9800000.00', '40833.00', '2026-02-26 10:02:58'),
(6, '2026-02-01', 'Đồng Nai', 70, 4, 190, '7600000.00', '40000.00', '2026-02-26 10:02:58'),
(7, '2026-02-01', 'Khánh Hòa', 55, 3, 150, '5800000.00', '38667.00', '2026-02-26 10:02:58'),
(8, '2026-02-01', 'Các tỉnh khác', 210, 15, 620, '22000000.00', '35484.00', '2026-02-26 10:02:58');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `inventory_snapshots`
--

DROP TABLE IF EXISTS `inventory_snapshots`;
CREATE TABLE IF NOT EXISTS `inventory_snapshots` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `snapshot_date` date NOT NULL,
  `medicine_id` bigint(20) NOT NULL,
  `medicine_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `quantity_on_hand` int(11) NOT NULL DEFAULT '0',
  `quantity_sold_today` int(11) NOT NULL DEFAULT '0',
  `quantity_received_today` int(11) NOT NULL DEFAULT '0',
  `stock_value` decimal(18,2) NOT NULL DEFAULT '0.00',
  `stock_status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL, LOW_STOCK, OUT_OF_STOCK, EXPIRING_SOON',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_inv_date_medicine` (`snapshot_date`,`medicine_id`),
  UNIQUE KEY `UK3k9aj8phmmfx2gnjl6935c4yw` (`snapshot_date`,`medicine_id`),
  KEY `idx_snapshot_date` (`snapshot_date`),
  KEY `idx_stock_status` (`stock_status`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `inventory_snapshots`
--

INSERT INTO `inventory_snapshots` (`id`, `snapshot_date`, `medicine_id`, `medicine_name`, `quantity_on_hand`, `quantity_sold_today`, `quantity_received_today`, `stock_value`, `stock_status`, `created_at`) VALUES
(1, '2026-02-26', 11, 'Amoxicillin 250mg (sắp HH)', 120, 5, 0, '1800000.00', 'EXPIRING_SOON', '2026-02-26 10:02:58'),
(2, '2026-02-26', 12, 'Cephalexin 500mg (sắp HH)', 80, 3, 0, '2400000.00', 'EXPIRING_SOON', '2026-02-26 10:02:58'),
(3, '2026-02-26', 13, 'Paracetamol 325mg (hết)', 0, 0, 0, '0.00', 'OUT_OF_STOCK', '2026-02-26 10:02:58'),
(4, '2026-02-26', 14, 'Vitamin B12 (sắp hết)', 15, 8, 0, '225000.00', 'LOW_STOCK', '2026-02-26 10:02:58'),
(5, '2026-03-04', 101, 'Paracetamol 500mg', 50, 5, 0, '500000.00', 'NORMAL', '2026-03-04 21:15:02'),
(6, '2026-03-04', 106, 'Omeprazole 20mg', 120, 12, 0, '3600000.00', 'NORMAL', '2026-03-04 21:15:02'),
(7, '2026-03-04', 107, 'Cetirizine 10mg', 200, 20, 0, '4000000.00', 'NORMAL', '2026-03-04 21:15:02'),
(8, '2026-03-04', 102, 'Amoxicillin 250mg', 8, 2, 0, '240000.00', 'LOW_STOCK', '2026-03-04 21:15:02'),
(9, '2026-03-04', 103, 'Vitamin C 1000mg', 5, 1, 0, '150000.00', 'LOW_STOCK', '2026-03-04 21:15:02'),
(10, '2026-03-04', 105, 'Metformin 500mg', 3, 0, 0, '90000.00', 'LOW_STOCK', '2026-03-04 21:15:02'),
(11, '2026-03-04', 104, 'Ibuprofen 400mg', 0, 0, 0, '0.00', 'OUT_OF_STOCK', '2026-03-04 21:15:02'),
(12, '2026-03-04', 108, 'Aspirin 81mg', 30, 3, 0, '450000.00', 'EXPIRING_SOON', '2026-03-04 21:15:02'),
(13, '2026-03-04', 109, 'Clopidogrel 75mg', 20, 2, 0, '1000000.00', 'EXPIRING_SOON', '2026-03-04 21:15:02'),
(14, '2026-03-04', 110, 'Atorvastatin 20mg', 10, 1, 0, '800000.00', 'EXPIRING_SOON', '2026-03-04 21:15:02');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `purchase_snapshots`
--

DROP TABLE IF EXISTS `purchase_snapshots`;
CREATE TABLE IF NOT EXISTS `purchase_snapshots` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `report_date` date NOT NULL,
  `total_orders` int(11) NOT NULL DEFAULT '0',
  `completed_orders` int(11) NOT NULL DEFAULT '0',
  `pending_orders` int(11) NOT NULL DEFAULT '0',
  `total_purchase_value` decimal(18,2) NOT NULL DEFAULT '0.00',
  `total_items_received` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `report_date` (`report_date`),
  UNIQUE KEY `UK21b271ecsyfi5votu72tptbyf` (`report_date`),
  KEY `idx_report_date` (`report_date`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `purchase_snapshots`
--

INSERT INTO `purchase_snapshots` (`id`, `report_date`, `total_orders`, `completed_orders`, `pending_orders`, `total_purchase_value`, `total_items_received`, `created_at`, `updated_at`) VALUES
(1, '2026-02-26', 3, 2, 1, '25000000.00', 150, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(2, '2026-02-25', 5, 5, 0, '42000000.00', 280, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(3, '2026-02-24', 2, 1, 1, '18000000.00', 90, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(4, '2026-02-23', 4, 4, 0, '35000000.00', 200, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(5, '2026-03-01', 15, 12, 3, '150000000.00', 1500, '2026-03-04 21:15:02', '2026-03-04 21:15:02');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `sales_snapshots`
--

DROP TABLE IF EXISTS `sales_snapshots`;
CREATE TABLE IF NOT EXISTS `sales_snapshots` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `report_date` date NOT NULL,
  `order_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'RETAIL, WHOLESALE, ONLINE, ALL',
  `total_invoices` int(11) NOT NULL DEFAULT '0',
  `paid_invoices` int(11) NOT NULL DEFAULT '0',
  `cancelled_invoices` int(11) NOT NULL DEFAULT '0',
  `total_revenue` decimal(18,2) NOT NULL DEFAULT '0.00',
  `total_discount` decimal(18,2) NOT NULL DEFAULT '0.00',
  `net_revenue` decimal(18,2) NOT NULL DEFAULT '0.00',
  `total_items_sold` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_sales_date_type` (`report_date`,`order_type`),
  UNIQUE KEY `UK9frih7eex52ntnh5p7dtobdpy` (`report_date`,`order_type`),
  KEY `idx_report_date` (`report_date`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `sales_snapshots`
--

INSERT INTO `sales_snapshots` (`id`, `report_date`, `order_type`, `total_invoices`, `paid_invoices`, `cancelled_invoices`, `total_revenue`, `total_discount`, `net_revenue`, `total_items_sold`, `created_at`, `updated_at`) VALUES
(1, '2026-02-26', 'ALL', 9, 42, 3, '20700000.00', '500000.00', '19800000.00', 220, '2026-02-26 10:02:48', '2026-03-04 21:15:02'),
(2, '2026-02-26', 'RETAIL', 30, 28, 2, '8000000.00', '200000.00', '7800000.00', 130, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(3, '2026-02-26', 'WHOLESALE', 10, 10, 0, '5500000.00', '200000.00', '5300000.00', 60, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(4, '2026-02-26', 'ONLINE', 5, 4, 1, '1500000.00', '100000.00', '1400000.00', 30, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(5, '2026-02-25', 'ALL', 14, 50, 2, '33000000.00', '700000.00', '31500000.00', 260, '2026-02-26 10:02:48', '2026-03-04 21:15:02'),
(6, '2026-02-25', 'RETAIL', 35, 33, 2, '10000000.00', '300000.00', '9700000.00', 160, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(7, '2026-02-25', 'WHOLESALE', 12, 12, 0, '6500000.00', '300000.00', '6200000.00', 70, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(8, '2026-02-25', 'ONLINE', 5, 5, 0, '2000000.00', '100000.00', '1900000.00', 30, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(9, '2026-02-24', 'ALL', 7, 37, 1, '16100000.00', '400000.00', '15400000.00', 190, '2026-02-26 10:02:48', '2026-03-04 21:15:02'),
(10, '2026-02-24', 'RETAIL', 25, 24, 1, '7000000.00', '200000.00', '6800000.00', 120, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(11, '2026-02-24', 'WHOLESALE', 9, 9, 0, '4800000.00', '150000.00', '4650000.00', 50, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(12, '2026-02-24', 'ONLINE', 4, 4, 0, '1200000.00', '50000.00', '1150000.00', 20, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(13, '2026-02-23', 'ALL', 13, 58, 3, '30200000.00', '800000.00', '28900000.00', 310, '2026-02-26 10:02:48', '2026-03-04 21:15:02'),
(14, '2026-02-23', 'RETAIL', 40, 38, 2, '11000000.00', '400000.00', '10600000.00', 190, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(15, '2026-02-23', 'WHOLESALE', 15, 14, 1, '7500000.00', '300000.00', '7200000.00', 90, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(16, '2026-02-23', 'ONLINE', 6, 6, 0, '2500000.00', '100000.00', '2400000.00', 30, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(17, '2026-02-22', 'ALL', 33, 31, 2, '11500000.00', '350000.00', '11150000.00', 165, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(18, '2026-02-22', 'RETAIL', 22, 21, 1, '6200000.00', '150000.00', '6050000.00', 100, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(19, '2026-02-22', 'WHOLESALE', 8, 7, 1, '4000000.00', '150000.00', '3850000.00', 45, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(20, '2026-02-22', 'ONLINE', 3, 3, 0, '1300000.00', '50000.00', '1250000.00', 20, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(21, '2026-02-21', 'ALL', 44, 43, 1, '15800000.00', '600000.00', '15200000.00', 230, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(22, '2026-02-21', 'RETAIL', 28, 27, 1, '8200000.00', '300000.00', '7900000.00', 140, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(23, '2026-02-21', 'WHOLESALE', 11, 11, 0, '5600000.00', '200000.00', '5400000.00', 65, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(24, '2026-02-21', 'ONLINE', 5, 5, 0, '2000000.00', '100000.00', '1900000.00', 25, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(25, '2026-02-20', 'ALL', 56, 53, 3, '19500000.00', '750000.00', '18750000.00', 280, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(26, '2026-02-20', 'RETAIL', 36, 34, 2, '10500000.00', '350000.00', '10150000.00', 170, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(27, '2026-02-20', 'WHOLESALE', 13, 13, 0, '6500000.00', '300000.00', '6200000.00', 80, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(28, '2026-02-20', 'ONLINE', 7, 6, 1, '2500000.00', '100000.00', '2400000.00', 30, '2026-02-26 10:02:48', '2026-02-26 10:02:48'),
(29, '2026-03-04', 'ALL', 12, 10, 1, '27000000.00', '1200000.00', '25800000.00', 85, '2026-03-04 21:15:02', '2026-03-04 21:15:02'),
(30, '2026-03-03', 'ALL', 8, 7, 0, '18200000.00', '700000.00', '17500000.00', 56, '2026-03-04 21:15:02', '2026-03-04 21:15:02'),
(31, '2026-03-02', 'ALL', 15, 13, 1, '35000000.00', '1800000.00', '33200000.00', 105, '2026-03-04 21:15:02', '2026-03-04 21:15:02'),
(32, '2026-03-01', 'ALL', 6, 5, 0, '12600000.00', '600000.00', '12000000.00', 42, '2026-03-04 21:15:02', '2026-03-04 21:15:02'),
(33, '2026-02-28', 'ALL', 10, 9, 0, '23000000.00', '900000.00', '22100000.00', 70, '2026-03-04 21:15:02', '2026-03-04 21:15:02'),
(34, '2026-02-27', 'ALL', 11, 10, 0, '25500000.00', '1200000.00', '24300000.00', 77, '2026-03-04 21:15:02', '2026-03-04 21:15:02'),
(35, '2026-02-01', 'ALL', 180, 162, 9, '410000000.00', '20000000.00', '390000000.00', 1260, '2026-03-04 21:15:02', '2026-03-04 21:15:02');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `top_medicine_reports`
--

DROP TABLE IF EXISTS `top_medicine_reports`;
CREATE TABLE IF NOT EXISTS `top_medicine_reports` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `report_month` date NOT NULL COMMENT 'Ngày đầu tháng',
  `medicine_id` bigint(20) NOT NULL,
  `medicine_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_quantity_sold` int(11) NOT NULL DEFAULT '0',
  `total_revenue` decimal(18,2) NOT NULL DEFAULT '0.00',
  `rank_in_month` int(11) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_top_month_medicine` (`report_month`,`medicine_id`),
  UNIQUE KEY `UKnwrpbe7dy69ljfa192kwq7l27` (`report_month`,`medicine_id`),
  KEY `idx_report_month` (`report_month`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `top_medicine_reports`
--

INSERT INTO `top_medicine_reports` (`id`, `report_month`, `medicine_id`, `medicine_name`, `category`, `total_quantity_sold`, `total_revenue`, `rank_in_month`, `created_at`) VALUES
(1, '2026-02-01', 1, 'Paracetamol 500mg', 'Giảm đau - Hạ sốt', 1250, '18750000.00', 1, '2026-02-26 10:02:48'),
(2, '2026-02-01', 2, 'Amoxicillin 500mg', 'Kháng sinh', 980, '29400000.00', 2, '2026-02-26 10:02:48'),
(3, '2026-02-01', 3, 'Vitamin C 1000mg', 'Vitamin & Khoáng chất', 850, '12750000.00', 3, '2026-02-26 10:02:48'),
(4, '2026-02-01', 4, 'Omeprazole 20mg', 'Tiêu hóa', 720, '21600000.00', 4, '2026-02-26 10:02:48'),
(5, '2026-02-01', 5, 'Cetirizine 10mg', 'Kháng dị ứng', 650, '9750000.00', 5, '2026-02-26 10:02:48'),
(6, '2026-02-01', 6, 'Metformin 500mg', 'Tiểu đường', 580, '8700000.00', 6, '2026-02-26 10:02:48'),
(7, '2026-02-01', 7, 'Ibuprofen 400mg', 'Giảm đau - Kháng viêm', 540, '8100000.00', 7, '2026-02-26 10:02:48'),
(8, '2026-02-01', 8, 'Atorvastatin 20mg', 'Tim mạch', 490, '14700000.00', 8, '2026-02-26 10:02:48'),
(9, '2026-02-01', 9, 'Losartan 50mg', 'Huyết áp', 450, '13500000.00', 9, '2026-02-26 10:02:48'),
(10, '2026-02-01', 10, 'Aspirin 81mg', 'Tim mạch', 420, '4200000.00', 10, '2026-02-26 10:02:48'),
(11, '2026-03-01', 101, 'Paracetamol 500mg', 'Giảm đau - Hạ sốt', 450, '45000000.00', 1, '2026-03-04 21:15:02'),
(12, '2026-03-01', 107, 'Cetirizine 10mg', 'Kháng histamine', 380, '38000000.00', 2, '2026-03-04 21:15:02'),
(13, '2026-03-01', 106, 'Omeprazole 20mg', 'Dạ dày', 320, '96000000.00', 3, '2026-03-04 21:15:02'),
(14, '2026-03-01', 102, 'Amoxicillin 250mg', 'Kháng sinh', 280, '28000000.00', 4, '2026-03-04 21:15:02'),
(15, '2026-03-01', 103, 'Vitamin C 1000mg', 'Vitamin & khoáng', 240, '24000000.00', 5, '2026-03-04 21:15:02');
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
