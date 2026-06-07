-- phpMyAdmin SQL Dump
-- version 5.0.2
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1:3306
-- Thời gian đã tạo: Th4 09, 2026 lúc 02:14 PM
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
  `ref_type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ref_id` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_res_ref` (`ref_type`,`ref_id`)
) ENGINE=InnoDB AUTO_INCREMENT=75 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `reservations`
--

INSERT INTO `reservations` (`id`, `ref_type`, `ref_id`, `status`, `created_by`, `created_at`) VALUES
(14, 'INVOICE', 'INV_DEMO_001', 'COMMITTED', 1, '2025-12-23 20:10:49'),
(20, 'INVOICE', 'inv_1766627303320', 'COMMITTED', 1, '2025-12-25 09:07:10'),
(24, 'INVOICE', 'INV-20260226-EE906', 'COMMITTED', 4, '2026-02-26 20:28:42'),
(25, 'INVOICE', 'INV-20260226-C848B', 'RELEASED', 4, '2026-02-26 20:31:29'),
(26, 'INVOICE', 'INV-20260305-62CDC', 'COMMITTED', 4, '2026-03-05 21:54:51'),
(27, 'INVOICE', 'INV-20260306-4411C', 'COMMITTED', 4, '2026-03-06 20:18:45'),
(28, 'BUYER_ORDER', '9', 'COMMITTED', 4, '2026-03-07 10:56:19'),
(29, 'BUYER_ORDER', '10', 'COMMITTED', 4, '2026-03-07 16:51:23'),
(30, 'BUYER_ORDER', '11', 'COMMITTED', 4, '2026-03-11 22:03:34'),
(31, 'BUYER_ORDER', '12', 'COMMITTED', 4, '2026-03-11 22:17:07'),
(32, 'BUYER_ORDER', '13', 'COMMITTED', 4, '2026-03-12 21:02:51'),
(33, 'BUYER_ORDER', '14', 'ACTIVE', 4, '2026-03-12 21:09:11'),
(34, 'BUYER_ORDER', '16', 'COMMITTED', 4, '2026-03-12 21:17:58'),
(35, 'BUYER_ORDER', '17', 'COMMITTED', 4, '2026-03-12 22:29:43'),
(36, 'BUYER_ORDER', '18', 'COMMITTED', 4, '2026-03-14 09:42:51'),
(37, 'BUYER_ORDER', '19', 'COMMITTED', 4, '2026-03-14 09:44:58'),
(38, 'BUYER_ORDER', '20', 'ACTIVE', 4, '2026-03-14 10:09:28'),
(39, 'BUYER_ORDER', '21', 'ACTIVE', 4, '2026-03-14 10:11:45'),
(40, 'BUYER_ORDER', '26', 'ACTIVE', 4, '2026-03-15 21:00:18'),
(45, 'BUYER_ORDER', '27', 'ACTIVE', 4, '2026-03-21 16:21:18'),
(46, 'BUYER_ORDER', '29', 'COMMITTED', 4, '2026-03-21 16:22:10'),
(48, 'INVOICE', 'INV-20260322-DA26B', 'RELEASED', 4, '2026-03-22 09:56:42'),
(51, 'INVOICE', 'INV-20260322-A235B', 'RELEASED', 4, '2026-03-22 10:06:41'),
(55, 'INVOICE', 'INV-20260322-F775E', 'COMMITTED', 4, '2026-03-22 16:41:03'),
(56, 'BUYER_ORDER', '31', 'COMMITTED', 4, '2026-03-28 20:26:26'),
(73, 'BUYER_ORDER', '34', 'COMMITTED', 4, '2026-03-30 22:10:18'),
(74, 'BUYER_ORDER', '35', 'COMMITTED', 4, '2026-04-06 21:32:48');

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
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
(24, 25, 2, 42, 1),
(25, 26, 1, 40, 9),
(26, 27, 1, 40, 1),
(27, 28, 1, 40, 5),
(28, 29, 1, 40, 1),
(29, 30, 1, 40, 10),
(30, 31, 1, 40, 11),
(31, 32, 1, 40, 3),
(32, 33, 1, 40, 1),
(33, 34, 1, 40, 5),
(34, 35, 1, 40, 22),
(35, 36, 1, 40, 4),
(36, 37, 1, 40, 1),
(37, 38, 1, 40, 3),
(38, 39, 1, 40, 1),
(39, 40, 1, 40, 7),
(44, 45, 1, 40, 6),
(45, 46, 1, 40, 3),
(47, 48, 1, 40, 1),
(48, 48, 3, 44, 1),
(49, 51, 1, 40, 1),
(50, 55, 1, 40, 1),
(51, 56, 3, 44, 1),
(52, 73, 5, 52, 1),
(53, 74, 3, 59, 2),
(54, 74, 4, 56, 1);

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
  `lot_number` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expiry_date` date NOT NULL,
  `import_price` decimal(38,2) NOT NULL,
  `qty_on_hand` int(11) NOT NULL DEFAULT '0',
  `qty_reserved` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `medicine_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lot` (`medicine_id`,`lot_number`,`expiry_date`),
  KEY `idx_lot_med_exp` (`medicine_id`,`expiry_date`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `stock_lots`
--

INSERT INTO `stock_lots` (`id`, `medicine_id`, `lot_number`, `expiry_date`, `import_price`, `qty_on_hand`, `qty_reserved`, `created_at`, `medicine_name`) VALUES
(39, 1, 'AMX_LO_01', '2025-12-31', '1200.00', 0, 0, '2025-12-23 20:10:45', 'amoxicillin 501mg'),
(40, 1, 'AMX_LO_02', '2026-06-30', '1250.00', 133, 18, '2025-12-23 20:10:45', 'amoxicillin 501mg'),
(41, 2, 'PARA_LO_01', '2025-10-15', '600.00', 0, 0, '2025-12-23 20:10:45', 'paracetamol 500mg'),
(42, 2, 'PARA_LO_02', '2026-03-20', '650.00', 0, 0, '2025-12-23 20:10:45', 'paracetamol 500mg'),
(43, 1, 'LO_PO_UPDATED', '2026-11-30', '600.00', 5, 0, '2025-12-23 22:08:43', 'amoxicillin 501mg'),
(44, 3, 'LO_RECEIVE_001', '2026-12-31', '500.00', 17, 0, '2025-12-23 22:08:43', 'paracetamol 500 test updated'),
(45, 4, '39', '2026-01-02', '200000.00', 0, 0, '2025-12-24 21:39:09', '12'),
(46, 1, '39', '2026-01-03', '100000.00', 0, 0, '2025-12-24 21:39:09', 'amoxicillin 501mg'),
(47, 1, '1', '2026-03-22', '12.00', 0, 0, '2026-03-22 10:26:19', 'amoxicillin 501mg'),
(48, 2, '1', '2026-03-22', '120.00', 0, 0, '2026-03-22 10:26:19', 'paracetamol 500mg'),
(49, 5, '1', '2026-03-22', '1110.00', 0, 0, '2026-03-22 10:26:19', 'SRM SIMPLE'),
(50, 4, '1', '2026-03-22', '110.00', 0, 0, '2026-03-22 10:26:19', '12'),
(51, 3, '1', '2026-03-22', '110.00', 0, 0, '2026-03-22 10:26:19', 'paracetamol 500 test updated'),
(52, 5, '1A', '2027-03-30', '10.00', 110, 0, '2026-03-30 22:09:42', 'SRM SIMPLE'),
(53, 4, 'A1', '2027-11-06', '200.00', 100, 0, '2026-04-06 21:25:09', '12'),
(54, 3, 'A2', '2028-08-06', '100.00', 100, 0, '2026-04-06 21:25:09', 'paracetamol 500 test updated'),
(55, 1, 'a3', '2029-03-06', '300.00', 101, 0, '2026-04-06 21:25:09', 'amoxicillin 501mg'),
(56, 4, 'Aa', '2026-06-06', '111.00', 199, 0, '2026-04-06 21:25:59', '12'),
(57, 1, 'a2', '2026-07-06', '111.00', 200, 0, '2026-04-06 21:27:58', 'amoxicillin 501mg'),
(58, 2, 'aa', '2026-08-06', '2000.00', 100, 0, '2026-04-06 21:31:32', NULL),
(59, 3, 'A1', '2026-11-06', '3000.00', 109, 0, '2026-04-06 21:31:32', NULL),
(60, 4, 'A3', '2026-07-03', '2000.00', 111, 0, '2026-04-06 21:31:32', NULL),
(61, 6, 'B2', '2026-11-21', '5000.00', 101, 0, '2026-04-06 21:31:32', NULL);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `stock_transactions`
--

DROP TABLE IF EXISTS `stock_transactions`;
CREATE TABLE IF NOT EXISTS `stock_transactions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ref_type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ref_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `medicine_id` bigint(20) NOT NULL,
  `lot_id` bigint(20) DEFAULT NULL,
  `qty` int(11) NOT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `performed_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tx_ref` (`ref_type`,`ref_id`),
  KEY `idx_tx_med` (`medicine_id`),
  KEY `idx_tx_lot` (`lot_id`)
) ENGINE=InnoDB AUTO_INCREMENT=128 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
(48, 'RESERVE', 'INVOICE', 'INV-20260226-C848B', 2, 42, 1, 'reserve', 4, '2026-02-26 20:31:29'),
(49, 'DAMAGED', NULL, NULL, 2, 41, 150, 'get á', 4, '2026-03-05 20:58:14'),
(50, 'DAMAGED', NULL, NULL, 1, 39, 97, 'sad', 4, '2026-03-05 20:58:59'),
(51, 'RESERVE', 'INVOICE', 'INV-20260305-62CDC', 1, 40, 9, 'reserve', 4, '2026-03-05 21:54:51'),
(52, 'COMMIT', 'INVOICE', 'INV-20260305-62CDC', 1, 40, 9, 'commit', 4, '2026-03-05 21:54:55'),
(53, 'RESERVE', 'INVOICE', 'INV-20260306-4411C', 1, 40, 1, 'reserve', 4, '2026-03-06 20:18:45'),
(54, 'COMMIT', 'INVOICE', 'INV-20260306-4411C', 1, 40, 1, 'commit', 4, '2026-03-06 20:18:48'),
(55, 'RESERVE', 'BUYER_ORDER', '9', 1, 40, 5, 'reserve', 4, '2026-03-07 10:56:19'),
(56, 'COMMIT', 'BUYER_ORDER', '9', 1, 40, 5, 'commit', 4, '2026-03-07 10:56:22'),
(57, 'RETURN', 'BUYER_ORDER', '9', 1, 40, 5, 'cancel-return', 4, '2026-03-07 10:56:28'),
(58, 'RESERVE', 'BUYER_ORDER', '10', 1, 40, 1, 'reserve', 4, '2026-03-07 16:51:23'),
(59, 'COMMIT', 'BUYER_ORDER', '10', 1, 40, 1, 'commit', 4, '2026-03-07 16:52:20'),
(60, 'RESERVE', 'BUYER_ORDER', '11', 1, 40, 10, 'reserve', 4, '2026-03-11 22:03:35'),
(61, 'COMMIT', 'BUYER_ORDER', '11', 1, 40, 10, 'commit', 4, '2026-03-11 22:03:59'),
(62, 'RESERVE', 'BUYER_ORDER', '12', 1, 40, 11, 'reserve', 4, '2026-03-11 22:17:07'),
(63, 'COMMIT', 'BUYER_ORDER', '12', 1, 40, 11, 'commit', 4, '2026-03-11 22:17:38'),
(64, 'RESERVE', 'BUYER_ORDER', '13', 1, 40, 3, 'reserve', 4, '2026-03-12 21:02:51'),
(65, 'COMMIT', 'BUYER_ORDER', '13', 1, 40, 3, 'commit', 4, '2026-03-12 21:03:12'),
(66, 'RESERVE', 'BUYER_ORDER', '14', 1, 40, 1, 'reserve', 4, '2026-03-12 21:09:11'),
(67, 'RESERVE', 'BUYER_ORDER', '16', 1, 40, 5, 'reserve', 4, '2026-03-12 21:17:58'),
(68, 'COMMIT', 'BUYER_ORDER', '16', 1, 40, 5, 'commit', 4, '2026-03-12 21:18:05'),
(69, 'RETURN', 'BUYER_ORDER', '16', 1, 40, 5, 'cancel-return', 4, '2026-03-12 21:20:19'),
(70, 'RESERVE', 'BUYER_ORDER', '17', 1, 40, 22, 'reserve', 4, '2026-03-12 22:29:43'),
(71, 'COMMIT', 'BUYER_ORDER', '17', 1, 40, 22, 'commit', 4, '2026-03-12 22:29:57'),
(72, 'RESERVE', 'BUYER_ORDER', '18', 1, 40, 4, 'reserve', 4, '2026-03-14 09:42:51'),
(73, 'COMMIT', 'BUYER_ORDER', '18', 1, 40, 4, 'commit', 4, '2026-03-14 09:43:27'),
(74, 'RESERVE', 'BUYER_ORDER', '19', 1, 40, 1, 'reserve', 4, '2026-03-14 09:44:58'),
(75, 'COMMIT', 'BUYER_ORDER', '19', 1, 40, 1, 'commit', 4, '2026-03-14 09:45:27'),
(76, 'RESERVE', 'BUYER_ORDER', '20', 1, 40, 3, 'reserve', 4, '2026-03-14 10:09:28'),
(77, 'RESERVE', 'BUYER_ORDER', '21', 1, 40, 1, 'reserve', 4, '2026-03-14 10:11:45'),
(78, 'RESERVE', 'BUYER_ORDER', '26', 1, 40, 7, 'reserve', 4, '2026-03-15 21:00:18'),
(83, 'RESERVE', 'BUYER_ORDER', '27', 1, 40, 6, 'reserve', 4, '2026-03-21 16:21:18'),
(84, 'RESERVE', 'BUYER_ORDER', '29', 1, 40, 3, 'reserve', 4, '2026-03-21 16:22:10'),
(85, 'COMMIT', 'BUYER_ORDER', '29', 1, 40, 3, 'commit', 4, '2026-03-21 16:22:27'),
(87, 'RESERVE', 'INVOICE', 'INV-20260322-DA26B', 1, 40, 1, 'reserve', 4, '2026-03-22 09:56:42'),
(88, 'RESERVE', 'INVOICE', 'INV-20260322-DA26B', 3, 44, 1, 'reserve', 4, '2026-03-22 09:56:42'),
(89, 'RESERVE', 'INVOICE', 'INV-20260322-A235B', 1, 40, 1, 'reserve', 4, '2026-03-22 10:06:41'),
(90, 'IN', 'PURCHASE', 'PO-20260322101238-2605', 1, 47, 111, 'inbound from purchase receive', 4, '2026-03-22 10:26:19'),
(91, 'IN', 'PURCHASE', 'PO-20260322101238-2605', 2, 48, 111, 'inbound from purchase receive', 4, '2026-03-22 10:26:19'),
(92, 'IN', 'PURCHASE', 'PO-20260322101238-2605', 5, 49, 221, 'inbound from purchase receive', 4, '2026-03-22 10:26:19'),
(93, 'IN', 'PURCHASE', 'PO-20260322101238-2605', 4, 50, 111, 'inbound from purchase receive', 4, '2026-03-22 10:26:19'),
(94, 'IN', 'PURCHASE', 'PO-20260322101238-2605', 3, 51, 221, 'inbound from purchase receive', 4, '2026-03-22 10:26:19'),
(95, 'RELEASE', 'INVOICE', 'INV-20260226-C848B', 2, 42, 1, 'release', 0, '2026-03-22 10:55:06'),
(96, 'RELEASE', 'INVOICE', 'INV-20260322-DA26B', 1, 40, 1, 'release', 0, '2026-03-22 10:55:06'),
(97, 'RELEASE', 'INVOICE', 'INV-20260322-DA26B', 3, 44, 1, 'release', 0, '2026-03-22 10:55:06'),
(98, 'RELEASE', 'INVOICE', 'INV-20260322-A235B', 1, 40, 1, 'release', 0, '2026-03-22 10:55:06'),
(99, 'RESERVE', 'INVOICE', 'INV-20260322-F775E', 1, 40, 1, 'reserve', 4, '2026-03-22 16:41:03'),
(100, 'COMMIT', 'INVOICE', 'INV-20260322-F775E', 1, 40, 1, 'commit', 4, '2026-03-22 16:41:07'),
(101, 'RESERVE', 'BUYER_ORDER', '31', 3, 44, 1, 'reserve', 4, '2026-03-28 20:26:26'),
(102, 'COMMIT', 'BUYER_ORDER', '31', 3, 44, 1, 'commit', 4, '2026-03-28 20:26:34'),
(103, 'IN', 'PURCHASE', 'PO-20260330220936-5316', 5, 52, 111, 'inbound from purchase receive', 4, '2026-03-30 22:09:43'),
(104, 'RESERVE', 'BUYER_ORDER', '34', 5, 52, 1, 'reserve', 4, '2026-03-30 22:10:18'),
(105, 'EXPIRED', NULL, NULL, 4, 45, 4, 'Hết hạn', 4, '2026-03-31 21:04:23'),
(106, 'EXPIRED', NULL, NULL, 1, 46, 9, 'Hết hạn', 4, '2026-03-31 21:04:28'),
(107, 'EXPIRED', NULL, NULL, 2, 42, 298, 'Hết hạn', 4, '2026-03-31 21:04:36'),
(108, 'EXPIRED', NULL, NULL, 1, 47, 111, 'Hết hạn', 4, '2026-03-31 21:04:39'),
(109, 'EXPIRED', NULL, NULL, 2, 48, 111, 'Hết hạn', 4, '2026-03-31 21:04:43'),
(110, 'EXPIRED', NULL, NULL, 5, 49, 221, 'Hết hạn', 4, '2026-03-31 21:04:46'),
(111, 'EXPIRED', NULL, NULL, 4, 50, 111, 'Hết hạn', 4, '2026-03-31 21:04:48'),
(112, 'EXPIRED', NULL, NULL, 3, 51, 221, 'Hết hạn', 4, '2026-03-31 21:04:51'),
(113, 'COMMIT', 'BUYER_ORDER', '34', 5, 52, 1, 'commit', 4, '2026-04-06 21:08:47'),
(114, 'DAMAGED', NULL, NULL, 1, 40, 1, 'Hư hỏng', 4, '2026-04-06 21:17:19'),
(115, 'IN', 'PURCHASE', 'PO-20260406212415-5875', 4, 53, 100, 'inbound from purchase receive', 4, '2026-04-06 21:25:09'),
(116, 'IN', 'PURCHASE', 'PO-20260406212415-5875', 3, 54, 100, 'inbound from purchase receive', 4, '2026-04-06 21:25:09'),
(117, 'IN', 'PURCHASE', 'PO-20260406212415-5875', 1, 55, 101, 'inbound from purchase receive', 4, '2026-04-06 21:25:09'),
(118, 'IN', 'PURCHASE', 'PO-20260406212553-5526', 4, 56, 200, 'inbound from purchase receive', 4, '2026-04-06 21:25:59'),
(119, 'IN', 'PURCHASE', 'PO-20260406212752-2118', 1, 57, 200, 'inbound from purchase receive', 4, '2026-04-06 21:27:58'),
(120, 'IN', 'PURCHASE', 'PO-20260406213127-0342', 2, 58, 100, 'inbound from purchase receive', 4, '2026-04-06 21:31:32'),
(121, 'IN', 'PURCHASE', 'PO-20260406213127-0342', 3, 59, 111, 'inbound from purchase receive', 4, '2026-04-06 21:31:32'),
(122, 'IN', 'PURCHASE', 'PO-20260406213127-0342', 4, 60, 111, 'inbound from purchase receive', 4, '2026-04-06 21:31:32'),
(123, 'IN', 'PURCHASE', 'PO-20260406213127-0342', 6, 61, 101, 'inbound from purchase receive', 4, '2026-04-06 21:31:32'),
(124, 'RESERVE', 'BUYER_ORDER', '35', 3, 59, 2, 'reserve', 4, '2026-04-06 21:32:48'),
(125, 'RESERVE', 'BUYER_ORDER', '35', 4, 56, 1, 'reserve', 4, '2026-04-06 21:32:48'),
(126, 'COMMIT', 'BUYER_ORDER', '35', 3, 59, 2, 'commit', 4, '2026-04-06 21:33:14'),
(127, 'COMMIT', 'BUYER_ORDER', '35', 4, 56, 1, 'commit', 4, '2026-04-06 21:33:14');

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
