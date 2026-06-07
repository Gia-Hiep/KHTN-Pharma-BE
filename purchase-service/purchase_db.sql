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
-- Cơ sở dữ liệu: `purchase_db`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `purchases`
--

DROP TABLE IF EXISTS `purchases`;
CREATE TABLE IF NOT EXISTS `purchases` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `supplier_id` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) NOT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `total` decimal(38,2) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `received_at` datetime DEFAULT NULL,
  `notes` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_p_supplier` (`supplier_id`),
  KEY `idx_p_created` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `purchases`
--

INSERT INTO `purchases` (`id`, `code`, `supplier_id`, `created_by`, `status`, `total`, `created_at`, `received_at`, `notes`) VALUES
(2, 'PO_0001', 1, 1, 'RECEIVED', '520000.00', '2025-12-21 20:37:11', '2025-12-21 20:37:51', 'nhap hang test'),
(5, 'PO_0002', 2, 1, 'RECEIVED', '400000.00', '2025-12-23 09:48:40', '2025-12-23 09:55:40', 'test inbound'),
(6, 'PO_0003', 3, 1, 'RECEIVED', '400000.00', '2025-12-23 09:56:56', '2025-12-23 09:57:10', 'test inbound'),
(7, 'PO_DEMO_001', 1, 1, 'CANCELLED', '13000.00', '2025-12-23 22:07:22', '2025-12-23 22:08:44', 'khong nhap nua'),
(8, 'pn_1766586949744', 3, 1, 'RECEIVED', '2700000.00', '2025-12-24 21:35:55', '2025-12-24 21:39:09', NULL),
(9, 'PO-20260322101238-2605', 5, 4, 'RECEIVED', '296482.00', '2026-03-22 10:12:39', '2026-03-22 10:26:20', 'sad'),
(10, 'PO-20260330220936-5316', 3, 4, 'RECEIVED', '1110.00', '2026-03-30 22:09:36', '2026-03-30 22:09:43', 'sda'),
(11, 'PO-20260406212415-5875', 5, 4, 'RECEIVED', '60300.00', '2026-04-06 21:24:16', '2026-04-06 21:25:09', NULL),
(12, 'PO-20260406212553-5526', 5, 4, 'RECEIVED', '22200.00', '2026-04-06 21:25:53', '2026-04-06 21:25:59', NULL),
(13, 'PO-20260406212752-2118', 3, 4, 'RECEIVED', '22200.00', '2026-04-06 21:27:53', '2026-04-06 21:27:58', NULL),
(14, 'PO-20260406213127-0342', 5, 4, 'RECEIVED', '1260000.00', '2026-04-06 21:31:27', '2026-04-06 21:31:32', NULL);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `purchase_items`
--

DROP TABLE IF EXISTS `purchase_items`;
CREATE TABLE IF NOT EXISTS `purchase_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `purchase_id` bigint(20) NOT NULL,
  `medicine_id` bigint(20) NOT NULL,
  `lot_number` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expiry_date` date NOT NULL,
  `import_price` decimal(38,2) NOT NULL,
  `qty` int(11) NOT NULL,
  `line_total` decimal(38,2) NOT NULL,
  `medicine_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pi_purchase` (`purchase_id`),
  KEY `idx_pi_med` (`medicine_id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `purchase_items`
--

INSERT INTO `purchase_items` (`id`, `purchase_id`, `medicine_id`, `lot_number`, `expiry_date`, `import_price`, `qty`, `line_total`, `medicine_name`) VALUES
(4, 2, 1, 'AMX_LO_01', '2025-12-31', '1200.00', 100, '120000.00', NULL),
(5, 2, 1, 'AMX_LO_02', '2026-06-30', '1250.00', 200, '250000.00', NULL),
(6, 2, 2, 'PARA_LO_01', '2025-10-15', '600.00', 150, '90000.00', NULL),
(7, 2, 1, 'lo_para_01', '2026-06-30', '600.00', 100, '60000.00', NULL),
(8, 5, 1, 'lo_para_01', '2026-06-30', '600.00', 100, '60000.00', NULL),
(9, 5, 1, 'AMX_LO_02', '2026-06-30', '1250.00', 200, '250000.00', NULL),
(10, 5, 2, 'PARA_LO_01', '2025-10-15', '600.00', 150, '90000.00', NULL),
(11, 6, 1, 'lo_para_01', '2026-06-30', '600.00', 100, '60000.00', NULL),
(12, 6, 1, 'AMX_LO_02', '2026-06-30', '1250.00', 200, '250000.00', NULL),
(13, 6, 2, 'PARA_LO_01', '2025-10-15', '600.00', 150, '90000.00', NULL),
(14, 7, 1, 'LO_PO_UPDATED', '2026-11-30', '600.00', 5, '3000.00', NULL),
(15, 7, 1, 'LO_RECEIVE_001', '2026-12-31', '500.00', 20, '10000.00', NULL),
(16, 8, 1, '39', '2026-01-02', '200000.00', 9, '1800000.00', NULL),
(17, 8, 1, '39', '2026-01-03', '100000.00', 9, '900000.00', NULL),
(18, 9, 1, '1', '2026-03-22', '12.00', 111, '1332.00', 'amoxicillin 501mg'),
(19, 9, 2, '1', '2026-03-22', '120.00', 111, '13320.00', 'paracetamol 500mg'),
(20, 9, 5, '1', '2026-03-22', '1110.00', 221, '245310.00', 'SRM SIMPLE'),
(21, 9, 4, '1', '2026-03-22', '110.00', 111, '12210.00', '12'),
(22, 9, 3, '1', '2026-03-22', '110.00', 221, '24310.00', 'paracetamol 500 test updated'),
(23, 10, 5, '1A', '2027-03-30', '10.00', 111, '1110.00', 'SRM SIMPLE'),
(24, 11, 4, 'A1', '2027-11-06', '200.00', 100, '20000.00', '12'),
(25, 11, 3, 'A2', '2028-08-06', '100.00', 100, '10000.00', 'paracetamol 500 test updated'),
(26, 11, 1, 'a3', '2029-03-06', '300.00', 101, '30300.00', 'amoxicillin 501mg'),
(27, 12, 4, 'Aa', '2026-06-06', '111.00', 200, '22200.00', '12'),
(28, 13, 1, 'a2', '2026-07-06', '111.00', 200, '22200.00', 'amoxicillin 501mg'),
(29, 14, 2, 'aa', '2026-08-06', '2000.00', 100, '200000.00', 'paracetamol 500mg'),
(30, 14, 3, 'A1', '2026-11-06', '3000.00', 111, '333000.00', 'paracetamol 500 test updated'),
(31, 14, 4, 'A3', '2026-07-03', '2000.00', 111, '222000.00', '12'),
(32, 14, 6, 'B2', '2026-11-21', '5000.00', 101, '505000.00', 'GEL NHA DAM');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `suppliers`
--

DROP TABLE IF EXISTS `suppliers`;
CREATE TABLE IF NOT EXISTS `suppliers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `contact_person` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `notes` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `suppliers`
--

INSERT INTO `suppliers` (`id`, `code`, `name`, `contact_person`, `phone`, `email`, `address`, `notes`, `created_at`) VALUES
(3, 'ncc', 'gia hiep', 'gia hiep', '0564082621', 'hiepcc@gmail.com', 'phu yen', 'test', '2025-12-24 21:34:34'),
(5, 'ncc_test', 'nha cung cap test', 'nguyen van a', '0900000000', 'a@test.com', 'quy nhon', 'supplier test', '2025-12-21 20:37:00'),
(6, NULL, 'adas', NULL, '111111111', 'sadas@gmail.com', 'sadas', NULL, '2026-03-06 20:21:52');

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `purchase_items`
--
ALTER TABLE `purchase_items`
  ADD CONSTRAINT `fk_pi_purchase` FOREIGN KEY (`purchase_id`) REFERENCES `purchases` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
