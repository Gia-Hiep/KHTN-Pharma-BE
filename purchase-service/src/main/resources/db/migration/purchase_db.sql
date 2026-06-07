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
-- Cơ sở dữ liệu: `purchase_db`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `purchases`
--

DROP TABLE IF EXISTS `purchases`;
CREATE TABLE IF NOT EXISTS `purchases` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `supplier_id` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) NOT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT',
  `total` decimal(15,2) NOT NULL DEFAULT '0.00',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `received_at` datetime DEFAULT NULL,
  `notes` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_p_supplier` (`supplier_id`),
  KEY `idx_p_created` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `purchases`
--

INSERT INTO `purchases` (`id`, `code`, `supplier_id`, `created_by`, `status`, `total`, `created_at`, `received_at`, `notes`) VALUES
(2, 'PO_0001', 1, 1, 'RECEIVED', '520000.00', '2025-12-21 20:37:11', '2025-12-21 20:37:51', 'nhap hang test'),
(5, 'PO_0002', 2, 1, 'RECEIVED', '400000.00', '2025-12-23 09:48:40', '2025-12-23 09:55:40', 'test inbound'),
(6, 'PO_0003', 3, 1, 'RECEIVED', '400000.00', '2025-12-23 09:56:56', '2025-12-23 09:57:10', 'test inbound'),
(7, 'PO_DEMO_001', 1, 1, 'CANCELLED', '13000.00', '2025-12-23 22:07:22', '2025-12-23 22:08:44', 'khong nhap nua'),
(8, 'pn_1766586949744', 3, 1, 'RECEIVED', '2700000.00', '2025-12-24 21:35:55', '2025-12-24 21:39:09', NULL);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `purchase_items`
--

DROP TABLE IF EXISTS `purchase_items`;
CREATE TABLE IF NOT EXISTS `purchase_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `purchase_id` bigint(20) NOT NULL,
  `medicine_id` bigint(20) NOT NULL,
  `lot_number` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expiry_date` date NOT NULL,
  `import_price` decimal(15,2) NOT NULL,
  `qty` int(11) NOT NULL,
  `line_total` decimal(15,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pi_purchase` (`purchase_id`),
  KEY `idx_pi_med` (`medicine_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `purchase_items`
--

INSERT INTO `purchase_items` (`id`, `purchase_id`, `medicine_id`, `lot_number`, `expiry_date`, `import_price`, `qty`, `line_total`) VALUES
(4, 2, 1, 'AMX_LO_01', '2025-12-31', '1200.00', 100, '120000.00'),
(5, 2, 1, 'AMX_LO_02', '2026-06-30', '1250.00', 200, '250000.00'),
(6, 2, 2, 'PARA_LO_01', '2025-10-15', '600.00', 150, '90000.00'),
(7, 2, 1, 'lo_para_01', '2026-06-30', '600.00', 100, '60000.00'),
(8, 5, 1, 'lo_para_01', '2026-06-30', '600.00', 100, '60000.00'),
(9, 5, 1, 'AMX_LO_02', '2026-06-30', '1250.00', 200, '250000.00'),
(10, 5, 2, 'PARA_LO_01', '2025-10-15', '600.00', 150, '90000.00'),
(11, 6, 1, 'lo_para_01', '2026-06-30', '600.00', 100, '60000.00'),
(12, 6, 1, 'AMX_LO_02', '2026-06-30', '1250.00', 200, '250000.00'),
(13, 6, 2, 'PARA_LO_01', '2025-10-15', '600.00', 150, '90000.00'),
(14, 7, 1, 'LO_PO_UPDATED', '2026-11-30', '600.00', 5, '3000.00'),
(15, 7, 1, 'LO_RECEIVE_001', '2026-12-31', '500.00', 20, '10000.00'),
(16, 8, 1, '39', '2026-01-02', '200000.00', 9, '1800000.00'),
(17, 8, 1, '39', '2026-01-03', '100000.00', 9, '900000.00');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `suppliers`
--

DROP TABLE IF EXISTS `suppliers`;
CREATE TABLE IF NOT EXISTS `suppliers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(40) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `contact_person` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` text COLLATE utf8mb4_unicode_ci,
  `notes` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `suppliers`
--

INSERT INTO `suppliers` (`id`, `code`, `name`, `contact_person`, `phone`, `email`, `address`, `notes`, `created_at`) VALUES
(3, 'ncc', 'gia hiep', 'gia hiep', '0564082621', 'hiepcc@gmail.com', 'phu yen', 'test', '2025-12-24 21:34:34'),
(5, 'ncc_test', 'nha cung cap test', 'nguyen van a', '0900000000', 'a@test.com', 'quy nhon', 'supplier test', '2025-12-21 20:37:00');

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
