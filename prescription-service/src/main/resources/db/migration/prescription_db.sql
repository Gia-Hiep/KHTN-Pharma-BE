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
-- Cơ sở dữ liệu: `prescription_db`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `prescriptions`
--

DROP TABLE IF EXISTS `prescriptions`;
CREATE TABLE IF NOT EXISTS `prescriptions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `prescription_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `customer_id` bigint(20) DEFAULT NULL,
  `customer_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `doctor_name` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `diagnosis` text COLLATE utf8mb4_unicode_ci,
  `prescription_date` date DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `notes` text COLLATE utf8mb4_unicode_ci,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `prescription_code` (`prescription_code`),
  KEY `idx_pr_phone` (`customer_phone`),
  KEY `idx_pr_date` (`prescription_date`),
  KEY `idx_pres_customer_phone` (`customer_phone`),
  KEY `idx_pres_status` (`status`),
  KEY `idx_pr_customer_id` (`customer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `prescriptions`
--

INSERT INTO `prescriptions` (`id`, `prescription_code`, `customer_id`, `customer_phone`, `doctor_name`, `diagnosis`, `prescription_date`, `status`, `notes`, `created_by`, `created_at`) VALUES
(4, 'PR_DEMO_001', 4, '0900000000', 'bac si a', 'cam cum', '2025-12-23', 'PROCESSING', 'uống sau ăn', 1, '2025-12-23 22:26:07'),
(5, 'rx_1766630394495', 2, '0901000002', 'Gia Hiep', 'viem gan', '2025-12-25', 'COMPLETED', 'Uong sau an', 1, '2025-12-25 09:40:35'),
(6, 'rx_1768007092294', NULL, '', 'aaa', 'ssdfvdfb', '2026-01-10', 'COMPLETED', 'sddsvsfvsfv', 4, '2026-01-10 08:05:22');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `prescription_items`
--

DROP TABLE IF EXISTS `prescription_items`;
CREATE TABLE IF NOT EXISTS `prescription_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `prescription_id` bigint(20) NOT NULL,
  `medicine_id` bigint(20) NOT NULL,
  `qty` int(11) NOT NULL,
  `dosage_instructions` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idx_pri_pr` (`prescription_id`),
  KEY `idx_pri_med` (`medicine_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `prescription_items`
--

INSERT INTO `prescription_items` (`id`, `prescription_id`, `medicine_id`, `qty`, `dosage_instructions`) VALUES
(4, 4, 1, 2, '1 vien x 2 lan/ngay'),
(5, 4, 2, 2, '2 vien x 3 lan/ ngay'),
(6, 4, 6, 1, 'asd'),
(7, 5, 1, 1, '2 vien x ngay'),
(8, 6, 1, 100, 'thee thuoc');

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `prescription_items`
--
ALTER TABLE `prescription_items`
  ADD CONSTRAINT `fk_pri_pr` FOREIGN KEY (`prescription_id`) REFERENCES `prescriptions` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
