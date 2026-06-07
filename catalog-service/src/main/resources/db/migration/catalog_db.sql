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
-- Cơ sở dữ liệu: `catalog_db`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `categories`
--

DROP TABLE IF EXISTS `categories`;
CREATE TABLE IF NOT EXISTS `categories` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `categories`
--

INSERT INTO `categories` (`id`, `code`, `name`, `description`) VALUES
(1, 'khang_sinh', 'khang sinh', NULL),
(2, 'giam_dau', 'giam dau', NULL),
(3, 'tieu_hoa', 'Tieu hoa', NULL),
(4, 'giam_dau_test', 'giam dau test', 'category seed for test'),
(5, 'asdsa', 'asdsda', 'adssda');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `disease_groups`
--

DROP TABLE IF EXISTS `disease_groups`;
CREATE TABLE IF NOT EXISTS `disease_groups` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `keywords` text COLLATE utf8mb4_unicode_ci COMMENT 'Comma-separated keywords cho chatbot matching',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_disease_group_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `disease_groups`
--

INSERT INTO `disease_groups` (`id`, `code`, `name`, `description`, `keywords`) VALUES
(1, 'tieu_hoa', 'Tiêu hóa', 'Các bệnh liên quan đến đường tiêu hóa', 'đau bụng,tiêu chảy,táo bón,khó tiêu,đầy hơi,ợ chua'),
(2, 'ho_hap', 'Hô hấp', 'Các bệnh liên quan đến đường hô hấp', 'ho,cảm cúm,viêm họng,sổ mũi,nghẹt mũi,khó thở'),
(3, 'tim_mach', 'Tim mạch', 'Các bệnh liên quan đến tim mạch', 'huyết áp,tim,huyết áp cao,cholesterol'),
(4, 'dau_nhuc', 'Đau nhức', 'Các triệu chứng đau nhức cơ thể', 'đau đầu,đau lưng,đau khớp,đau cơ,sốt'),
(5, 'da_lieu', 'Da liễu', 'Các bệnh liên quan đến da', 'ngứa,dị ứng,mẩn đỏ,viêm da,nấm'),
(6, 'mat_tai', 'Mắt - Tai', 'Các bệnh liên quan đến mắt và tai', 'đau mắt,khô mắt,viêm tai,đau tai'),
(7, 'than_kinh', 'Thần kinh', 'Các vấn đề về thần kinh', 'mất ngủ,stress,lo âu,căng thẳng,đau đầu');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `medicines`
--

DROP TABLE IF EXISTS `medicines`;
CREATE TABLE IF NOT EXISTS `medicines` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `generic_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `unit` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_rx` tinyint(1) NOT NULL DEFAULT '0',
  `manufacturer` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category_id` bigint(20) DEFAULT NULL,
  `default_supplier_id` bigint(20) DEFAULT NULL,
  `sale_price` decimal(38,2) NOT NULL,
  `barcode` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `usage_instructions` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `side_effects` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `active_ingredient` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dosage_form` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `origin` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `package_size` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  UNIQUE KEY `uk_m_barcode` (`barcode`),
  KEY `idx_m_cat` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `medicines`
--

INSERT INTO `medicines` (`id`, `code`, `name`, `generic_name`, `unit`, `is_rx`, `manufacturer`, `category_id`, `default_supplier_id`, `sale_price`, `barcode`, `image_url`, `description`, `usage_instructions`, `side_effects`, `status`, `created_at`, `updated_at`, `active_ingredient`, `dosage_form`, `origin`, `package_size`) VALUES
(1, 'paracetamol_500', 'amoxicillin 501mg', 'paracetamol', 'vien', 0, 'abc pharma', 2, NULL, '2000.00', '893000000001', NULL, 'adsdsa', NULL, NULL, 'ACTIVE', '2025-12-21 08:26:28', '2025-12-24 16:47:39', NULL, NULL, NULL, NULL),
(2, 'amoxicillin_500', 'paracetamol 500mg', 'amoxicillin', 'vien', 1, 'xyz pharma', 1, NULL, '2000.00', '893000000002', NULL, NULL, NULL, NULL, 'ACTIVE', '2025-12-21 08:26:28', '2025-12-22 08:53:37', NULL, NULL, NULL, NULL),
(3, 'para_test_500', 'paracetamol 500 test updated', 'paracetamol', 'vien', 0, 'abc pharma updated', 4, 1, '1800.00', '893000009999', NULL, 'thuoc test updated', '1 vien x 3 lan/ngay', 'buon ngu', 'ACTIVE', '2025-12-23 08:18:52', '2025-12-23 08:19:18', NULL, NULL, NULL, NULL),
(4, '1', '12', '132', 'vien', 0, '2', 1, NULL, '2222.00', '123', NULL, '21', '32', '12', 'ACTIVE', '2025-12-24 16:48:52', '2025-12-25 19:16:05', NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `medicine_disease_groups`
--

DROP TABLE IF EXISTS `medicine_disease_groups`;
CREATE TABLE IF NOT EXISTS `medicine_disease_groups` (
  `medicine_id` bigint(20) NOT NULL,
  `disease_group_id` bigint(20) NOT NULL,
  PRIMARY KEY (`medicine_id`,`disease_group_id`),
  KEY `idx_mdg_medicine` (`medicine_id`),
  KEY `idx_mdg_disease_group` (`disease_group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `medicine_disease_groups`
--

INSERT INTO `medicine_disease_groups` (`medicine_id`, `disease_group_id`) VALUES
(1, 4),
(2, 2);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `medicine_price_history`
--

DROP TABLE IF EXISTS `medicine_price_history`;
CREATE TABLE IF NOT EXISTS `medicine_price_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `medicine_id` bigint(20) NOT NULL,
  `old_price` decimal(38,2) NOT NULL,
  `new_price` decimal(38,2) NOT NULL,
  `changed_by` bigint(20) DEFAULT NULL,
  `changed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_mph_med` (`medicine_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `medicine_price_history`
--

INSERT INTO `medicine_price_history` (`id`, `medicine_id`, `old_price`, `new_price`, `changed_by`, `changed_at`) VALUES
(1, 3, '1200.00', '1500.00', 1, '2025-12-23 08:19:14'),
(2, 3, '1500.00', '1800.00', 1, '2025-12-23 08:19:18'),
(3, 1, '1000.00', '2000.00', 1, '2025-12-24 16:39:05'),
(4, 4, '2.00', '2222.00', 1, '2025-12-24 16:49:23');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `medicine_pricing_tiers`
--

DROP TABLE IF EXISTS `medicine_pricing_tiers`;
CREATE TABLE IF NOT EXISTS `medicine_pricing_tiers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `medicine_id` bigint(20) NOT NULL,
  `tier_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'RETAIL, WHOLESALE, VIP, GOLD, SILVER',
  `min_qty` int(11) NOT NULL DEFAULT '1' COMMENT 'Số lượng tối thiểu để áp dụng giá này',
  `price` decimal(15,2) NOT NULL,
  `discount_percent` decimal(5,2) DEFAULT '0.00',
  `effective_from` datetime NOT NULL,
  `effective_to` datetime DEFAULT NULL COMMENT 'NULL = vô thời hạn',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pricing_tier` (`medicine_id`,`tier_code`,`effective_from`),
  KEY `idx_pricing_medicine` (`medicine_id`),
  KEY `idx_pricing_tier_code` (`tier_code`),
  KEY `idx_pricing_effective` (`effective_from`,`effective_to`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `medicine_pricing_tiers`
--

INSERT INTO `medicine_pricing_tiers` (`id`, `medicine_id`, `tier_code`, `min_qty`, `price`, `discount_percent`, `effective_from`, `effective_to`, `created_at`, `created_by`) VALUES
(1, 1, 'RETAIL', 1, '2000.00', '0.00', '2026-02-08 20:36:18', NULL, '2026-02-08 20:36:18', 1),
(2, 1, 'WHOLESALE', 100, '1800.00', '10.00', '2026-02-08 20:36:18', NULL, '2026-02-08 20:36:18', 1),
(3, 1, 'VIP', 1, '1900.00', '5.00', '2026-02-08 20:36:18', NULL, '2026-02-08 20:36:18', 1);

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `medicines`
--
ALTER TABLE `medicines` ADD FULLTEXT KEY `ftx_m_name_generic` (`name`,`generic_name`);

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `medicines`
--
ALTER TABLE `medicines`
  ADD CONSTRAINT `fk_m_cat` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`);

--
-- Các ràng buộc cho bảng `medicine_disease_groups`
--
ALTER TABLE `medicine_disease_groups`
  ADD CONSTRAINT `fk_mdg_disease_group` FOREIGN KEY (`disease_group_id`) REFERENCES `disease_groups` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_mdg_medicine` FOREIGN KEY (`medicine_id`) REFERENCES `medicines` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `medicine_pricing_tiers`
--
ALTER TABLE `medicine_pricing_tiers`
  ADD CONSTRAINT `fk_pricing_medicine` FOREIGN KEY (`medicine_id`) REFERENCES `medicines` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
