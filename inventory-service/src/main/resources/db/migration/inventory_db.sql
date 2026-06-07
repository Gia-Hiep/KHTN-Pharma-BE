-- phpMyAdmin SQL Dump
-- version 5.0.2
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1:3306
-- Thời gian đã tạo: Th3 02, 2026 lúc 01:18 PM
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
-- Cơ sở dữ liệu: `inventory_db`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `reservations`
--

DROP TABLE IF EXISTS `reservations`;
CREATE TABLE IF NOT EXISTS `reservations` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ref_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ref_id` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_res_ref` (`ref_type`,`ref_id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `reservations`
--

INSERT INTO `reservations` (`id`, `ref_type`, `ref_id`, `status`, `created_by`, `created_at`) VALUES
(14, 'INVOICE', 'INV_DEMO_001', 'COMMITTED', 1, '2025-12-23 20:10:49'),
(20, 'INVOICE', 'inv_1766627303320', 'COMMITTED', 1, '2025-12-25 09:07:10'),
(24, 'INVOICE', 'INV-20260226-EE906', 'COMMITTED', 4, '2026-02-26 20:28:42'),
(25, 'INVOICE', 'INV-20260226-C848B', 'ACTIVE', 4, '2026-02-26 20:31:29');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `reservation_items`
--

DROP TABLE IF EXISTS `reservation_items`;
CREATE TABLE IF NOT EXISTS `reservation_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `reservation_id` bigint(20) NOT NULL,
  `medicine_id` bigint(20) NOT NULL,
  `lot_id` bigint(20) NOT NULL,
  `qty` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_ri_res` (`reservation_id`),
  KEY `idx_ri_lot` (`lot_id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `reservation_items`
--

INSERT INTO `reservation_items` (`id`, `reservation_id`, `medicine_id`, `lot_id`, `qty`) VALUES
(3, 14, 1, 39, 2),
(4, 14, 1, 39, 2),
(14, 20, 1, 39, 1),
(15, 20, 2, 42, 1),
(16, 20, 3, 44, 1),
(17, 20, 4, 45, 1),
(18, 20, 4, 45, 4),
(22, 24, 3, 44, 1),
(23, 24, 2, 42, 1),
(24, 25, 2, 42, 1);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `stock_alert_rules`
--

DROP TABLE IF EXISTS `stock_alert_rules`;
CREATE TABLE IF NOT EXISTS `stock_alert_rules` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `medicine_id` bigint(20) NOT NULL,
  `min_stock_level` int(11) NOT NULL DEFAULT '10',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `medicine_id` (`medicine_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `stock_alert_rules`
--

INSERT INTO `stock_alert_rules` (`id`, `medicine_id`, `min_stock_level`, `updated_at`) VALUES
(1, 1, 50, '2025-12-21 09:38:51'),
(2, 2, 100, '2025-12-21 09:38:51');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `stock_lots`
--

DROP TABLE IF EXISTS `stock_lots`;
CREATE TABLE IF NOT EXISTS `stock_lots` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `medicine_id` bigint(20) NOT NULL,
  `lot_number` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expiry_date` date NOT NULL,
  `import_price` decimal(15,2) NOT NULL DEFAULT '0.00',
  `qty_on_hand` int(11) NOT NULL DEFAULT '0',
  `qty_reserved` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lot` (`medicine_id`,`lot_number`,`expiry_date`),
  KEY `idx_lot_med_exp` (`medicine_id`,`expiry_date`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `stock_lots`
--

INSERT INTO `stock_lots` (`id`, `medicine_id`, `lot_number`, `expiry_date`, `import_price`, `qty_on_hand`, `qty_reserved`, `created_at`) VALUES
(39, 1, 'AMX_LO_01', '2025-12-31', '1200.00', 97, 0, '2025-12-23 20:10:45'),
(40, 1, 'AMX_LO_02', '2026-06-30', '1250.00', 200, 0, '2025-12-23 20:10:45'),
(41, 2, 'PARA_LO_01', '2025-10-15', '600.00', 150, 0, '2025-12-23 20:10:45'),
(42, 2, 'PARA_LO_02', '2026-03-20', '650.00', 298, 1, '2025-12-23 20:10:45'),
(43, 1, 'LO_PO_UPDATED', '2026-11-30', '600.00', 5, 0, '2025-12-23 22:08:43'),
(44, 3, 'LO_RECEIVE_001', '2026-12-31', '500.00', 18, 0, '2025-12-23 22:08:43'),
(45, 4, '39', '2026-01-02', '200000.00', 4, 0, '2025-12-24 21:39:09'),
(46, 1, '39', '2026-01-03', '100000.00', 9, 0, '2025-12-24 21:39:09');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `stock_transactions`
--

DROP TABLE IF EXISTS `stock_transactions`;
CREATE TABLE IF NOT EXISTS `stock_transactions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ref_type` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ref_id` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `medicine_id` bigint(20) NOT NULL,
  `lot_id` bigint(20) DEFAULT NULL,
  `qty` int(11) NOT NULL,
  `note` text COLLATE utf8mb4_unicode_ci,
  `performed_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tx_ref` (`ref_type`,`ref_id`),
  KEY `idx_tx_med` (`medicine_id`),
  KEY `idx_tx_lot` (`lot_id`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `stock_transactions`
--

INSERT INTO `stock_transactions` (`id`, `type`, `ref_type`, `ref_id`, `medicine_id`, `lot_id`, `qty`, `note`, `performed_by`, `created_at`) VALUES
(10, 'RESERVE', 'INVOICE', 'INV_DEMO_001', 1, 39, 2, 'reserve', 1, '2025-12-23 20:10:49'),
(11, 'RESERVE', 'INVOICE', 'INV_DEMO_001', 1, 39, 2, 'reserve', 1, '2025-12-23 20:11:26'),
(12, 'COMMIT', 'INVOICE', 'INV_DEMO_001', 1, 39, 2, 'commit', 1, '2025-12-23 20:11:29'),
(13, 'COMMIT', 'INVOICE', 'INV_DEMO_001', 1, 39, 2, 'commit', 1, '2025-12-23 20:11:29'),
(14, 'IN', 'PURCHASE', 'PO_DEMO_001', 1, 43, 5, 'inbound from purchase receive', 1, '2025-12-23 22:08:43'),
(15, 'IN', 'PURCHASE', 'PO_DEMO_001', 1, 44, 20, 'inbound from purchase receive', 1, '2025-12-23 22:08:43'),
(16, 'ADJUST', NULL, NULL, 1, 39, 3, 'asda', 1, '2025-12-24 21:01:02'),
(17, 'ADJUST', NULL, NULL, 1, 39, 1, '1', 1, '2025-12-24 21:01:21'),
(18, 'DAMAGED', NULL, NULL, 1, 39, 1, NULL, 1, '2025-12-24 21:04:03'),
(19, 'EXPIRED', NULL, NULL, 1, 39, 1, NULL, 1, '2025-12-24 21:04:36'),
(20, 'IN', 'PURCHASE', 'pn_1766586949744', 1, 45, 9, 'inbound from purchase receive', 1, '2025-12-24 21:39:09'),
(21, 'IN', 'PURCHASE', 'pn_1766586949744', 1, 46, 9, 'inbound from purchase receive', 1, '2025-12-24 21:39:09'),
(31, 'RESERVE', 'INVOICE', 'inv_1766627303320', 1, 39, 1, 'reserve', 1, '2025-12-25 09:07:10'),
(32, 'RESERVE', 'INVOICE', 'inv_1766627303320', 2, 42, 1, 'reserve', 1, '2025-12-25 09:07:10'),
(33, 'RESERVE', 'INVOICE', 'inv_1766627303320', 3, 44, 1, 'reserve', 1, '2025-12-25 09:07:10'),
(34, 'RESERVE', 'INVOICE', 'inv_1766627303320', 4, 45, 1, 'reserve', 1, '2025-12-25 09:07:10'),
(35, 'RESERVE', 'INVOICE', 'inv_1766627303320', 4, 45, 4, 'reserve', 1, '2025-12-25 09:07:10'),
(36, 'COMMIT', 'INVOICE', 'inv_1766627303320', 1, 39, 1, 'commit', 1, '2025-12-25 09:08:56'),
(37, 'COMMIT', 'INVOICE', 'inv_1766627303320', 2, 42, 1, 'commit', 1, '2025-12-25 09:08:56'),
(38, 'COMMIT', 'INVOICE', 'inv_1766627303320', 3, 44, 1, 'commit', 1, '2025-12-25 09:08:56'),
(39, 'COMMIT', 'INVOICE', 'inv_1766627303320', 4, 45, 1, 'commit', 1, '2025-12-25 09:08:56'),
(40, 'COMMIT', 'INVOICE', 'inv_1766627303320', 4, 45, 4, 'commit', 1, '2025-12-25 09:08:56'),
(44, 'RESERVE', 'INVOICE', 'INV-20260226-EE906', 3, 44, 1, 'reserve', 4, '2026-02-26 20:28:42'),
(45, 'RESERVE', 'INVOICE', 'INV-20260226-EE906', 2, 42, 1, 'reserve', 4, '2026-02-26 20:28:42'),
(46, 'COMMIT', 'INVOICE', 'INV-20260226-EE906', 3, 44, 1, 'commit', 4, '2026-02-26 20:29:09'),
(47, 'COMMIT', 'INVOICE', 'INV-20260226-EE906', 2, 42, 1, 'commit', 4, '2026-02-26 20:29:09'),
(48, 'RESERVE', 'INVOICE', 'INV-20260226-C848B', 2, 42, 1, 'reserve', 4, '2026-02-26 20:31:29');

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `reservation_items`
--
ALTER TABLE `reservation_items`
  ADD CONSTRAINT `fk_ri_lot` FOREIGN KEY (`lot_id`) REFERENCES `stock_lots` (`id`),
  ADD CONSTRAINT `fk_ri_res` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
