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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `stock_lots`
--

INSERT INTO `stock_lots` (`id`, `medicine_id`, `lot_number`, `expiry_date`, `import_price`, `qty_on_hand`, `qty_reserved`, `created_at`, `medicine_name`) VALUES
(1, 1, 'LOT-PARA-01', '2027-12-31', '1200.00', 200, 0, '2026-01-15 10:00:00', 'Paracetamol 500mg'),
(2, 1, 'LOT-PARA-02', '2027-06-30', '1200.00', 3, 0, '2026-03-01 10:00:00', 'Paracetamol 500mg'),
(3, 2, 'LOT-IBUP-01', '2027-09-30', '2000.00', 150, 0, '2026-01-20 10:00:00', 'Ibuprofen 400mg'),
(4, 3, 'LOT-AMOX-01', '2027-11-30', '3000.00', 100, 0, '2026-01-10 10:00:00', 'Amoxicillin 500mg'),
(5, 3, 'LOT-AMOX-02', '2026-05-12', '3000.00', 50, 0, '2025-08-01 10:00:00', 'Amoxicillin 500mg'),
(6, 4, 'LOT-AZIT-01', '2028-03-31', '8000.00', 80, 0, '2026-02-01 10:00:00', 'Azithromycin 250mg'),
(7, 5, 'LOT-COLD-01', '2027-10-31', '2500.00', 200, 0, '2026-01-25 10:00:00', 'Coldacmin Forte'),
(8, 6, 'LOT-OMEP-01', '2028-01-31', '2800.00', 120, 0, '2026-02-05 10:00:00', 'Omeprazol 20mg'),
(9, 6, 'LOT-OMEP-02', '2027-08-31', '2800.00', 50, 0, '2025-12-10 10:00:00', 'Omeprazol 20mg'),
(10, 7, 'LOT-SMEC-01', '2027-07-31', '5000.00', 100, 0, '2026-01-18 10:00:00', 'Smecta 3g'),
(11, 8, 'LOT-AMLO-01', '2028-06-30', '1500.00', 150, 0, '2026-02-10 10:00:00', 'Amlodipin 5mg'),
(12, 8, 'LOT-AMLO-02', '2027-12-31', '1500.00', 80, 0, '2025-11-15 10:00:00', 'Amlodipin 5mg'),
(13, 9, 'LOT-VITC-01', '2027-05-31', '3000.00', 300, 0, '2026-01-05 10:00:00', 'Vitamin C 1000mg'),
(14, 10, 'LOT-OMG3-01', '2027-09-30', '8000.00', 80, 0, '2026-02-20 10:00:00', 'Omega 3-6-9'),
(15, 11, 'LOT-BETA-01', '2028-04-30', '22000.00', 60, 0, '2026-03-01 10:00:00', 'Betadine 10%'),
(16, 12, 'LOT-NACL-01', '2028-02-28', '9000.00', 100, 0, '2026-01-28 10:00:00', 'Natri Clorid 0.9%'),
(17, 12, 'LOT-NACL-02', '2027-06-30', '9000.00', 50, 0, '2025-10-20 10:00:00', 'Natri Clorid 0.9%'),
(18, 9, 'LOT-VITC-02', '2027-11-30', '3000.00', 100, 0, '2026-03-15 10:00:00', 'Vitamin C 1000mg'),
(19, 1, 'LOT-1', '2029-10-20', '2000.00', 1000, 0, '2026-04-20 21:47:26', 'Paracetamol 500mg');

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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `stock_transactions`
--

INSERT INTO `stock_transactions` (`id`, `type`, `ref_type`, `ref_id`, `medicine_id`, `lot_id`, `qty`, `note`, `performed_by`, `created_at`) VALUES
(1, 'IN', 'PURCHASE', 'PO-20260420214536-2360', 1, 19, 1000, 'inbound from purchase receive', 4, '2026-04-20 21:47:26');

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
