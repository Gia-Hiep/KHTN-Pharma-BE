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
-- Cơ sở dữ liệu: `customer_db`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `coupons`
--

DROP TABLE IF EXISTS `coupons`;
CREATE TABLE IF NOT EXISTS `coupons` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `discount_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'PERCENT or FIXED',
  `discount_value` decimal(15,2) NOT NULL,
  `min_order_amount` decimal(15,2) DEFAULT '0.00',
  `max_discount` decimal(15,2) DEFAULT NULL,
  `usage_limit` int(11) DEFAULT NULL COMMENT 'NULL = unlimited',
  `used_count` int(11) NOT NULL DEFAULT '0',
  `valid_from` datetime NOT NULL,
  `valid_to` datetime NOT NULL,
  `customer_tier` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'NULL = all tiers',
  `customer_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'NULL = all types',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_coupon_code` (`code`),
  KEY `idx_coupon_active` (`is_active`),
  KEY `idx_coupon_valid` (`valid_from`,`valid_to`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `coupons`
--

INSERT INTO `coupons` (`id`, `code`, `discount_type`, `discount_value`, `min_order_amount`, `max_discount`, `usage_limit`, `used_count`, `valid_from`, `valid_to`, `customer_tier`, `customer_type`, `is_active`, `description`, `created_at`, `created_by`) VALUES
(1, 'WELCOME10', 'PERCENT', '10.00', '100000.00', '50000.00', 100, 0, '2026-02-08 20:43:46', '2026-05-08 20:43:46', NULL, NULL, 1, 'Giảm 10% cho khách mới, tối đa 50k', '2026-02-08 20:43:46', NULL),
(2, 'PHARMACY20', 'PERCENT', '20.00', '1000000.00', '500000.00', NULL, 0, '2026-02-08 20:43:46', '2026-08-08 20:43:46', NULL, 'PHARMACY', 1, 'Dành cho nhà thuốc sỉ', '2026-02-08 20:43:46', NULL),
(3, 'FIXED100K', 'FIXED', '100000.00', '500000.00', NULL, 50, 0, '2026-02-08 20:43:46', '2026-03-08 20:43:46', NULL, NULL, 1, 'Giảm 100k cho đơn từ 500k', '2026-02-08 20:43:46', NULL);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `coupon_usage`
--

DROP TABLE IF EXISTS `coupon_usage`;
CREATE TABLE IF NOT EXISTS `coupon_usage` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `coupon_id` bigint(20) NOT NULL,
  `customer_id` bigint(20) NOT NULL,
  `invoice_id` bigint(20) NOT NULL,
  `discount_applied` decimal(15,2) NOT NULL,
  `used_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_usage_coupon` (`coupon_id`),
  KEY `idx_usage_customer` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `customers`
--

DROP TABLE IF EXISTS `customers`;
CREATE TABLE IF NOT EXISTS `customers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `full_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `gender` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `loyalty_points` int(11) NOT NULL DEFAULT '0',
  `notes` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `customer_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'INDIVIDUAL',
  `tier` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'REGULAR',
  `credit_limit` decimal(15,2) DEFAULT '0.00',
  `current_debt` decimal(15,2) DEFAULT '0.00',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `phone` (`phone`),
  UNIQUE KEY `idx_customer_phone` (`phone`),
  UNIQUE KEY `UKeuat1oase6eqv195jvb71a93s` (`user_id`),
  KEY `idx_cus_phone` (`phone`),
  KEY `idx_customer_type` (`customer_type`),
  KEY `idx_customer_tier` (`tier`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `customers`
--

INSERT INTO `customers` (`id`, `full_name`, `phone`, `email`, `address`, `date_of_birth`, `gender`, `loyalty_points`, `notes`, `customer_type`, `tier`, `credit_limit`, `current_debt`, `created_at`, `updated_at`, `user_id`) VALUES
(1, 'nguyen van a', '0901000011', 'a@gmail.com', 'quy nhons', NULL, NULL, 0, NULL, 'INDIVIDUAL', 'REGULAR', '0.00', '0.00', '2025-12-21 19:53:19', '2026-03-06 21:04:59', NULL),
(2, 'tran thi b', '0901000002', 'b@gmail.com', 'quy nhon', NULL, NULL, 0, NULL, 'INDIVIDUAL', 'REGULAR', '0.00', '0.00', '2025-12-21 19:53:19', '2025-12-22 09:10:16', NULL),
(4, 'nguyen van a', '0900000000', 'a@test.com', 'quy nhon - updated', '2003-01-01', 'MALE', 10, 'cap nhat', 'INDIVIDUAL', 'REGULAR', '0.00', '0.00', '2025-12-22 09:33:10', '2025-12-22 09:33:56', NULL),
(5, 'adsasaddsadsa', 'sadsda', 'asdsda', 'dsasa', NULL, NULL, 0, 'dsasad', 'INDIVIDUAL', 'REGULAR', '0.00', '0.00', '2025-12-24 16:21:37', '2025-12-24 16:21:42', NULL),
(6, 'PHAM GIA HIEP', 'saddsa', 'saddsa', '4642 Grant View Drive', NULL, NULL, 0, 'dsa', 'INDIVIDUAL', 'REGULAR', '0.00', '0.00', '2025-12-25 18:16:33', '2025-12-25 18:16:33', NULL),
(7, 'Nhà thuốc ABC1', '0901888888', 'abc@pharmacy.com2', '123 Nguyễn Huệ, Q1, HCM22', NULL, NULL, 5000, NULL, 'PHARMACY', 'GOLD', '50000000.00', '0.00', '2026-02-08 20:43:46', '2026-03-06 20:54:45', NULL),
(8, 'Anh hiệp', '0564082621', 'dsadsa', 'dsadsadsa', NULL, NULL, 0, 'created from sales-service', 'INDIVIDUAL', 'REGULAR', '0.00', '0.00', '2026-03-05 21:55:39', '2026-03-07 15:47:32', NULL),
(9, 'dsadas', '22222222', 'đá@gmail.com', 'dsada', NULL, NULL, 0, NULL, 'INDIVIDUAL', 'REGULAR', '0.00', '0.00', '2026-03-06 20:54:58', '2026-03-06 20:54:58', NULL),
(10, 'Buyer #6', 'TEMP_6', NULL, NULL, NULL, NULL, 0, NULL, 'INDIVIDUAL', 'REGULAR', '0.00', '0.00', '2026-03-21 16:07:48', '2026-03-21 16:07:48', 6),
(11, 'đâsd', 'áddsadas', 'dsadsads', 'dsasad', NULL, NULL, 0, NULL, 'INDIVIDUAL', 'REGULAR', '0.00', '0.00', '2026-03-21 16:14:19', '2026-03-21 16:14:19', NULL);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `customer_debts`
--

DROP TABLE IF EXISTS `customer_debts`;
CREATE TABLE IF NOT EXISTS `customer_debts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `customer_id` bigint(20) NOT NULL,
  `invoice_id` bigint(20) NOT NULL,
  `invoice_code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `amount` decimal(15,2) NOT NULL,
  `paid_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, PARTIAL, PAID, OVERDUE',
  `due_date` date NOT NULL,
  `notes` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_debt_customer` (`customer_id`),
  KEY `idx_debt_status` (`status`),
  KEY `idx_debt_due_date` (`due_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `loyalty_transactions`
--

DROP TABLE IF EXISTS `loyalty_transactions`;
CREATE TABLE IF NOT EXISTS `loyalty_transactions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `customer_id` bigint(20) NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'EARN, REDEEM, EXPIRE, ADJUST',
  `points` int(11) NOT NULL,
  `reference_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'INVOICE, PROMOTION, MANUAL, COUPON',
  `reference_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_loyalty_customer` (`customer_id`),
  KEY `idx_loyalty_type` (`type`),
  KEY `idx_loyalty_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `coupon_usage`
--
ALTER TABLE `coupon_usage`
  ADD CONSTRAINT `fk_usage_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_usage_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `customer_debts`
--
ALTER TABLE `customer_debts`
  ADD CONSTRAINT `fk_debt_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `loyalty_transactions`
--
ALTER TABLE `loyalty_transactions`
  ADD CONSTRAINT `fk_loyalty_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
