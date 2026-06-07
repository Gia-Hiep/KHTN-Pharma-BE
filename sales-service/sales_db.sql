-- phpMyAdmin SQL Dump
-- version 5.0.2
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1:3306
-- Thời gian đã tạo: Th4 09, 2026 lúc 02:15 PM
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
  `shipped_at` datetime(6) DEFAULT NULL,
  `tracking_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `buyer_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `payment_status` enum('PAID','REFUNDED','UNPAID') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `return_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `returned_at` datetime(6) DEFAULT NULL,
  `shipper_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shipper_phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `buyer_confirmed` tinyint(1) DEFAULT '0',
  `stripe_payment_intent_id` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `payment_transaction_id` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `buyer_orders`
--

INSERT INTO `buyer_orders` (`id`, `buyer_id`, `coupon_code`, `created_at`, `discount`, `notes`, `payment_method`, `processed_by`, `rejection_reason`, `shipping_address`, `status`, `subtotal`, `total`, `updated_at`, `carrier`, `delivered_at`, `shipped_at`, `tracking_code`, `buyer_name`, `payment_status`, `return_reason`, `returned_at`, `shipper_name`, `shipper_phone`, `buyer_confirmed`, `stripe_payment_intent_id`, `payment_transaction_id`) VALUES
(1, 6, NULL, '2026-02-26 22:02:12.966201', '0.00', 'dsasd', 'CASH', 4, NULL, 'adsdsa', 'DELIVERED', '8000.00', '8000.00', '2026-03-07 11:17:44.352441', NULL, NULL, NULL, NULL, 'saddsasda', NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL),
(2, 6, NULL, '2026-02-28 15:15:52.323450', '0.00', '', 'CASH', 4, NULL, 'dsasa', 'DELIVERED', '8000.00', '8000.00', '2026-03-07 11:17:44.352441', 'LIEN_TINH', '2026-03-07 09:59:50.014647', '2026-03-07 09:59:37.590399', 'GND', 'saddsasda', NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL),
(3, 6, NULL, '2026-03-01 19:54:53.624473', '0.00', 'dsa', 'CASH', 4, 'sd', 'sdasdasadas', 'REJECTED', '1800.00', '1800.00', '2026-03-07 11:17:44.352441', NULL, NULL, NULL, NULL, 'saddsasda', NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL),
(4, 6, 'dá', '2026-03-01 20:00:10.589119', '0.00', 'dsads', 'CASH', 4, 'PHARMACIST_CANCELLED', 'đasa', 'REJECTED', '404404.00', '404404.00', '2026-03-07 11:17:44.352441', NULL, NULL, NULL, NULL, 'saddsasda', NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL),
(5, 6, 'dsasda', '2026-03-01 20:29:46.349897', '0.00', 'adsads', 'CASH', 4, NULL, 'dsasda', 'DELIVERED', '224000.00', '224000.00', '2026-03-07 11:17:44.352441', 'GIAO_HANG_NHANH', '2026-03-01 20:30:23.258589', '2026-03-01 20:30:19.235441', 'GHN1123', 'saddsasda', NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL),
(6, 6, 'dsadsa', '2026-03-01 20:31:18.874209', '0.00', 'sad', 'CASH', 4, 'ko đủ hàng', 'dsa', 'REJECTED', '248864.00', '248864.00', '2026-03-07 11:17:44.352441', NULL, NULL, NULL, NULL, 'saddsasda', NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL),
(7, 6, 'dsa', '2026-03-01 20:32:44.640282', '0.00', 'ád', 'CASH', 4, NULL, 'dsa', 'DELIVERED', '222000.00', '222000.00', '2026-03-07 11:17:44.350339', 'GIAO_HANG_NHANH', '2026-03-01 21:01:05.821548', '2026-03-01 21:01:04.171619', 'jhkh', 'saddsasda', NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL),
(8, 6, 'sdasda', '2026-03-02 21:15:24.951227', '0.00', 'dsasad', 'CASH', NULL, 'BUYER_CANCELLED', 'adssad', 'REJECTED', '10000.00', '10000.00', '2026-03-07 11:17:44.350339', NULL, NULL, NULL, NULL, 'saddsasda', NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL),
(9, 6, NULL, '2026-03-07 10:28:01.304278', '0.00', 'saddsa', 'CASH', 4, 'PHARMACIST_CANCELLED', 'ấdas', 'REJECTED', '10000.00', '10000.00', '2026-03-07 11:17:44.348103', NULL, NULL, NULL, NULL, 'saddsasda', NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL),
(10, 6, NULL, '2026-03-07 16:37:36.279432', '0.00', '', 'CASH', 4, NULL, 'dsasda', 'PACKING', '2000.00', '2000.00', '2026-03-07 16:53:15.882493', NULL, NULL, NULL, NULL, 'saddsasda', NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL),
(11, 6, 'dsa', '2026-03-11 22:03:17.719543', '0.00', 'dsa\n[SHIPPING] sad', 'CASH', 4, NULL, 'asdsa', 'DELIVERED', '20000.00', '20000.00', '2026-03-11 22:04:16.991594', 'GIAO_HANG_NHANH', '2026-03-11 22:04:16.988438', '2026-03-11 22:04:10.127816', 'aasa', 'saddsasda', 'UNPAID', NULL, NULL, 'dasdsa', 'asdds', 0, NULL, NULL),
(12, 6, NULL, '2026-03-11 22:16:46.387372', '0.00', 'dsa\n[SHIPPING] dsa', 'CASH', 4, NULL, 'dasa', 'DELIVERED', '22000.00', '22000.00', '2026-03-11 22:23:00.812950', 'GIAO_HANG_NHANH', '2026-03-11 22:17:45.367425', '2026-03-11 22:17:42.896895', 'dá', 'saddsasda', 'PAID', NULL, NULL, 'dsa', 'sda', 1, NULL, NULL),
(13, 6, 'dsa', '2026-03-12 21:02:07.258546', '0.00', 'dsa\n[SHIPPING] dsa', 'CASH', 4, NULL, 'saddsa', 'DELIVERED', '9000.00', '9000.00', '2026-03-12 21:03:33.964723', 'GIAO_HANG_NHANH', '2026-03-12 21:03:30.804151', '2026-03-12 21:03:26.891397', 'dá', 'saddsasda', 'PAID', NULL, NULL, 'dsa', 'sad', 0, NULL, NULL),
(14, 6, NULL, '2026-03-12 21:08:50.341064', '0.00', 'dsa', 'CASH', 4, NULL, 'ádsa', 'PICKING', '2000.00', '2000.00', '2026-03-12 21:09:33.157193', NULL, NULL, NULL, NULL, 'saddsasda', 'UNPAID', NULL, NULL, NULL, NULL, 0, NULL, NULL),
(15, 6, 'đasa', '2026-03-12 21:16:21.587183', '0.00', 'adsasd', 'COD', NULL, 'BUYER_CANCELLED', 'dá', 'REJECTED', '6000.00', '6000.00', '2026-03-12 21:16:36.129939', NULL, NULL, NULL, NULL, 'saddsasda', 'UNPAID', NULL, NULL, NULL, NULL, 0, NULL, NULL),
(16, 6, NULL, '2026-03-12 21:17:37.651464', '0.00', '\n[SHIPPING] sad', 'COD', 4, 'ko nhận hàng', 'Cho phép thanh toán đa kênh (COD, chuyển khoản, ví điện tử, QR Pay).', 'REJECTED', '10000.00', '10000.00', '2026-03-12 21:20:19.158646', 'LIEN_TINH', NULL, '2026-03-12 21:19:56.073968', 'ádasd', 'saddsasda', 'UNPAID', NULL, NULL, 'adssda', 'adssda', 0, NULL, NULL),
(17, 6, NULL, '2026-03-12 22:29:32.813143', '0.00', 'dsasd\n[SHIPPING] sda', 'BANK_TRANSFER', 4, NULL, 'dsadsa', 'DELIVERED', '44000.00', '44000.00', '2026-03-12 22:30:50.273159', 'GIAO_HANG_NHANH', '2026-03-12 22:30:34.447925', '2026-03-12 22:30:31.742893', 'dsadsa', 'saddsasda', 'UNPAID', NULL, NULL, 'dsa', 'ád', 1, NULL, NULL),
(18, 6, 'dsa', '2026-03-14 09:42:41.295977', '0.00', '\n[SHIPPING] dsa', 'COD', 4, NULL, 'sdasad', 'DELIVERED', '8000.00', '8000.00', '2026-03-14 09:44:25.154765', 'GIAO_HANG_NHANH', '2026-03-14 09:43:57.995872', '2026-03-14 09:43:40.631957', 'das', 'saddsasda', 'PAID', NULL, NULL, 'dsa', 'dsa', 1, NULL, NULL),
(19, 6, NULL, '2026-03-14 09:44:41.080866', '0.00', '', 'BANK_TRANSFER', 4, NULL, 'dsasdasda', 'DELIVERED', '2000.00', '2000.00', '2026-03-14 09:45:36.757791', 'GIAO_HANG_NHANH', '2026-03-14 09:45:36.757791', '2026-03-14 09:45:33.205449', 'dsasd', 'saddsasda', 'UNPAID', NULL, NULL, 'adssda', 'asd', 0, NULL, NULL),
(20, 6, NULL, '2026-03-14 10:09:20.545012', '0.00', 'áddsa', 'BANK_TRANSFER', 4, NULL, 'dâsdasd', 'PICKING', '6000.00', '6000.00', '2026-03-14 10:10:51.080188', NULL, NULL, NULL, NULL, 'saddsasda', 'UNPAID', NULL, NULL, NULL, NULL, 0, NULL, NULL),
(21, 6, NULL, '2026-03-14 10:11:38.793269', '0.00', '', 'COD', 4, NULL, 'dsasadsda', 'PICKING', '2000.00', '2000.00', '2026-03-14 10:11:44.994183', NULL, NULL, NULL, NULL, 'saddsasda', 'UNPAID', NULL, NULL, NULL, NULL, 0, NULL, NULL),
(22, 6, NULL, '2026-03-14 10:12:12.231106', '0.00', 'adssda', 'COD', NULL, NULL, 'sdasd', 'PENDING_APPROVAL', '2000.00', '2000.00', NULL, NULL, NULL, NULL, NULL, 'saddsasda', 'UNPAID', NULL, NULL, NULL, NULL, 0, NULL, NULL),
(23, 6, NULL, '2026-03-14 10:12:24.231581', '0.00', 'ádsa', 'COD', NULL, NULL, 'dsasda', 'PENDING_APPROVAL', '6000.00', '6000.00', NULL, NULL, NULL, NULL, NULL, 'saddsasda', 'UNPAID', NULL, NULL, NULL, NULL, 0, NULL, NULL),
(24, 6, NULL, '2026-03-14 10:12:31.814677', '0.00', '', 'BANK_TRANSFER', NULL, NULL, 'sấd', 'PENDING_APPROVAL', '2000.00', '2000.00', NULL, NULL, NULL, NULL, NULL, 'saddsasda', 'UNPAID', NULL, NULL, NULL, NULL, 0, NULL, NULL),
(25, 6, NULL, '2026-03-14 10:35:19.846632', '0.00', '', 'COD', NULL, 'BUYER_CANCELLED', 'đá', 'REJECTED', '4000.00', '4000.00', '2026-03-14 10:35:22.237610', NULL, NULL, NULL, NULL, 'saddsasda', 'UNPAID', NULL, NULL, NULL, NULL, 0, NULL, NULL),
(26, 6, NULL, '2026-03-15 20:59:55.314062', '0.00', '', 'STRIPE', 4, NULL, 'dsasda', 'CONFIRMED', '14000.00', '14000.00', '2026-03-15 21:00:17.858587', NULL, NULL, NULL, NULL, 'saddsasda', 'PAID', NULL, NULL, NULL, NULL, 0, NULL, NULL),
(27, 6, NULL, '2026-03-16 21:17:59.864649', '0.00', '', 'BANK_TRANSFER', 4, NULL, 'dsadsa', 'PICKING', '12000.00', '12000.00', '2026-03-21 16:21:21.638054', NULL, NULL, NULL, NULL, 'saddsasda', 'PAID', NULL, NULL, NULL, NULL, 0, NULL, NULL),
(28, 6, NULL, '2026-03-21 16:08:32.483601', '0.00', 'saddsa', 'BANK_TRANSFER', 4, 'ád', 'saddsadsa', 'REJECTED', '4022.00', '4022.00', '2026-03-21 16:19:56.132258', NULL, NULL, NULL, NULL, 'saddsasda', 'PAID', NULL, NULL, NULL, NULL, 0, NULL, NULL),
(29, 6, NULL, '2026-03-21 16:22:01.152109', '0.00', '\n[SHIPPING] ads', 'BANK_TRANSFER', 4, NULL, 'dsa', 'DELIVERED', '6000.00', '6000.00', '2026-03-21 16:23:03.293594', 'GIAO_HANG_NHANH', '2026-03-21 16:22:33.698557', '2026-03-21 16:22:32.324114', 'dsa', 'saddsasda', 'PAID', NULL, NULL, 'dsa', 'dsa', 1, NULL, NULL),
(30, 6, NULL, '2026-03-24 21:56:26.498983', '0.00', 'das', 'BANK_TRANSFER', NULL, NULL, 'dsaadssad', 'PENDING_APPROVAL', '100000.00', '100000.00', NULL, NULL, NULL, NULL, NULL, 'saddsasda', 'PAID', NULL, NULL, NULL, NULL, 0, NULL, NULL),
(31, 6, NULL, '2026-03-28 20:26:15.768236', '0.00', '\n[SHIPPING] sda', 'COD', 4, NULL, 'đasa', 'DELIVERED', '1800.00', '1800.00', '2026-03-28 20:26:48.786216', 'LIEN_TINH', '2026-03-28 20:26:48.770310', '2026-03-28 20:26:45.678308', 'sda', 'saddsasda', 'PAID', NULL, NULL, 'dsa', 'dsa', 0, NULL, NULL),
(32, 6, NULL, '2026-03-30 19:31:29.963757', '0.00', '', 'COD', NULL, NULL, 'saddsadsa', 'PENDING_APPROVAL', '100000.00', '100000.00', NULL, NULL, NULL, NULL, NULL, 'saddsasda', 'UNPAID', NULL, NULL, NULL, NULL, 0, NULL, NULL),
(33, 6, NULL, '2026-03-30 19:37:51.379719', '0.00', '', 'COD', NULL, NULL, 'sdas', 'PENDING_APPROVAL', '100000.00', '100000.00', NULL, NULL, NULL, NULL, NULL, 'saddsasda', 'UNPAID', NULL, NULL, NULL, NULL, 0, NULL, NULL),
(34, 6, NULL, '2026-03-30 21:54:54.842503', '0.00', '', 'COD', 4, NULL, 'dsa', 'PACKING', '100000.00', '100000.00', '2026-04-06 21:08:47.212455', NULL, NULL, NULL, NULL, 'saddsasda', 'UNPAID', NULL, NULL, NULL, NULL, 0, NULL, NULL),
(35, 6, NULL, '2026-04-06 21:22:54.353923', '0.00', '', 'BANK_TRANSFER', 4, NULL, 'sadsaasdsa', 'PACKING', '5822.00', '5822.00', '2026-04-06 21:33:14.293995', NULL, NULL, NULL, NULL, 'saddsasda', 'PAID', NULL, NULL, NULL, NULL, 0, NULL, NULL);

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
  `fulfilled` bit(1) DEFAULT NULL,
  `original_qty` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3j6cm5mxk3fhs4crd3ge8ldpy` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `buyer_order_items`
--

INSERT INTO `buyer_order_items` (`id`, `line_total`, `medicine_id`, `medicine_name`, `price_tier`, `qty`, `unit_price`, `order_id`, `fulfilled`, `original_qty`) VALUES
(1, '8000.00', 1, 'amoxicillin 501mg', 'RETAIL', 4, '2000.00', 1, NULL, NULL),
(2, '8000.00', 1, 'amoxicillin 501mg', 'RETAIL', 4, '2000.00', 2, NULL, NULL),
(3, '1800.00', 1, 'amoxicillin 501mg', 'WHOLESALE', 1, '1800.00', 3, NULL, NULL),
(4, '404404.00', 4, '12', 'RETAIL', 182, '2222.00', 4, NULL, NULL),
(5, '224000.00', 2, 'paracetamol 500mg', 'RETAIL', 112, '2000.00', 5, NULL, NULL),
(6, '248864.00', 4, '12', 'RETAIL', 112, '2222.00', 6, NULL, NULL),
(7, '222000.00', 2, 'paracetamol 500mg', 'RETAIL', 111, '2000.00', 7, NULL, NULL),
(8, '10000.00', 1, 'amoxicillin 501mg', 'RETAIL', 5, '2000.00', 8, NULL, NULL),
(9, '10000.00', 1, 'amoxicillin 501mg', 'RETAIL', 5, '2000.00', 9, NULL, NULL),
(10, '2000.00', 1, 'amoxicillin 501mg', 'RETAIL', 1, '2000.00', 10, NULL, NULL),
(11, '20000.00', 1, 'amoxicillin 501mg', 'RETAIL', 10, '2000.00', 11, b'0', 10),
(12, '22000.00', 1, 'amoxicillin 501mg', 'RETAIL', 11, '2000.00', 12, b'1', 11),
(13, '9000.00', 1, 'amoxicillin 501mg', 'RETAIL', 3, '3000.00', 13, b'0', 3),
(14, '2000.00', 1, 'amoxicillin 501mg', 'RETAIL', 1, '2000.00', 14, b'0', 1),
(15, '6000.00', 1, 'amoxicillin 501mg', 'RETAIL', 3, '2000.00', 15, b'0', 3),
(16, '10000.00', 1, 'amoxicillin 501mg', 'RETAIL', 5, '2000.00', 16, b'1', 5),
(17, '44000.00', 1, 'amoxicillin 501mg', 'RETAIL', 22, '2000.00', 17, b'1', 22),
(18, '8000.00', 1, 'amoxicillin 501mg', 'RETAIL', 4, '2000.00', 18, b'0', 4),
(19, '2000.00', 1, 'amoxicillin 501mg', 'RETAIL', 1, '2000.00', 19, b'0', 1),
(20, '6000.00', 1, 'amoxicillin 501mg', 'RETAIL', 3, '2000.00', 20, b'0', 3),
(21, '2000.00', 1, 'amoxicillin 501mg', 'RETAIL', 1, '2000.00', 21, b'0', 1),
(22, '2000.00', 1, 'amoxicillin 501mg', 'RETAIL', 1, '2000.00', 22, b'0', 1),
(23, '6000.00', 2, 'paracetamol 500mg', 'RETAIL', 3, '2000.00', 23, b'0', 3),
(24, '2000.00', 1, 'amoxicillin 501mg', 'RETAIL', 1, '2000.00', 24, b'0', 1),
(25, '4000.00', 1, 'amoxicillin 501mg', 'RETAIL', 2, '2000.00', 25, b'0', 2),
(26, '14000.00', 1, 'amoxicillin 501mg', 'RETAIL', 7, '2000.00', 26, b'0', 7),
(27, '12000.00', 1, 'amoxicillin 501mg', 'RETAIL', 6, '2000.00', 27, b'0', 6),
(28, '1800.00', 3, 'paracetamol 500 test updated', 'RETAIL', 1, '1800.00', 28, b'0', 1),
(29, '2222.00', 4, '12', 'RETAIL', 1, '2222.00', 28, b'0', 1),
(30, '6000.00', 1, 'amoxicillin 501mg', 'RETAIL', 3, '2000.00', 29, b'1', 3),
(31, '100000.00', 5, 'SRM SIMPLE', 'RETAIL', 1, '100000.00', 30, b'0', 1),
(32, '1800.00', 3, 'paracetamol 500 test updated', 'RETAIL', 1, '1800.00', 31, b'1', 1),
(33, '100000.00', 5, 'SRM SIMPLE', 'RETAIL', 1, '100000.00', 32, b'0', 1),
(34, '100000.00', 5, 'SRM SIMPLE', 'RETAIL', 1, '100000.00', 33, b'0', 1),
(35, '100000.00', 5, 'SRM SIMPLE', 'RETAIL', 1, '100000.00', 34, b'1', 1),
(36, '3600.00', 3, 'paracetamol 500 test updated', 'RETAIL', 2, '1800.00', 35, b'1', 2),
(37, '2222.00', 4, '12', 'RETAIL', 1, '2222.00', 35, b'1', 1);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `carts`
--

DROP TABLE IF EXISTS `carts`;
CREATE TABLE IF NOT EXISTS `carts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `buyer_id` bigint(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKlhsynm4v4ttbdtlbgkmyl9bfy` (`buyer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `carts`
--

INSERT INTO `carts` (`id`, `buyer_id`, `created_at`, `updated_at`) VALUES
(1, 6, '2026-04-04 21:39:56.722823', '2026-04-04 21:39:56.847397'),
(2, 14, '2026-04-04 21:40:11.966868', '2026-04-04 21:40:11.974042');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `cart_items`
--

DROP TABLE IF EXISTS `cart_items`;
CREATE TABLE IF NOT EXISTS `cart_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `medicine_id` bigint(20) NOT NULL,
  `medicine_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `price_tier` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `qty` int(11) NOT NULL,
  `unit_price` decimal(38,2) DEFAULT NULL,
  `cart_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK65234xcuasjjm0rj2vkwr9f4i` (`cart_id`,`medicine_id`,`price_tier`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `cart_items`
--

INSERT INTO `cart_items` (`id`, `image_url`, `medicine_id`, `medicine_name`, `price_tier`, `qty`, `unit_price`, `cart_id`) VALUES
(3, '/uploads/medicines/287ebd6d-ee83-4e5d-b59a-8221eabd9979.png', 4, '12', 'RETAIL', 1, '2222.00', 2),
(4, '/uploads/medicines/6c389e72-abec-4cf0-9e77-7f8bede67956.png', 6, 'GEL NHA DAM', 'RETAIL', 1, '3333.00', 1);

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
  `coupon_code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `coupon_discount` decimal(15,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_inv_created` (`created_at`),
  KEY `idx_inv_cashier` (`cashier_id`),
  KEY `fk_inv_customer` (`customer_id`),
  KEY `idx_inv_order_type` (`order_type`),
  KEY `idx_inv_channel` (`channel`),
  KEY `idx_inv_shipping` (`requires_shipping`,`shipping_status`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `invoices`
--

INSERT INTO `invoices` (`id`, `code`, `customer_id`, `cashier_id`, `status`, `payment_status`, `subtotal`, `discount`, `total`, `created_at`, `updated_at`, `notes`, `order_type`, `channel`, `requires_shipping`, `shipping_address`, `shipping_fee`, `shipping_status`, `tracking_code`, `coupon_code`, `coupon_discount`) VALUES
(8, 'INV_DEMO_001', 4, 1, 'PAID', 'PAID', '2000.00', '0.00', '2000.00', '2026-01-10 07:16:48', '2026-01-10 07:18:41', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(9, 'inv_1766627303320', 4, 1, 'PAID', 'PAID', '16910.00', '0.00', '16910.00', '2026-01-10 20:11:19', '2026-01-10 20:11:19', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(10, 'inv_1766628689091', NULL, 1, 'DRAFT', 'UNPAID', '0.00', '0.00', '0.00', '2026-01-10 20:11:19', '2026-01-10 20:11:19', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(11, 'inv_1766632913511', 2, 1, 'DRAFT', 'UNPAID', '0.00', '0.00', '0.00', '2025-12-25 10:22:09', '2026-01-10 20:11:19', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(12, 'INV-20260109-232720', NULL, 3, 'DRAFT', 'UNPAID', '2000.00', '0.00', '2000.00', '2026-01-10 20:11:19', '2026-01-10 20:11:19', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(13, 'INV-20260109-233112', NULL, 3, 'DRAFT', 'UNPAID', '0.00', '0.00', '0.00', '2026-01-10 20:11:19', '2026-01-10 20:11:19', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(14, 'inv_1767976588677', NULL, 3, 'CANCELLED', 'UNPAID', '22000.00', '0.00', '22000.00', '2026-01-10 20:11:19', '2026-01-10 20:11:19', 'thich', 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(15, 'INV-20260109-234215', NULL, 3, 'DRAFT', 'UNPAID', '0.00', '0.00', '0.00', '2026-01-10 20:11:19', '2026-01-10 20:11:19', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(16, 'INV-20260109-235218', NULL, 3, 'DRAFT', 'UNPAID', '0.00', '0.00', '0.00', '2026-01-10 20:11:19', '2026-01-10 07:20:48', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(17, 'INV-20260226-503BE', 7, 4, 'DRAFT', 'UNPAID', '13110.00', '0.00', '13110.00', '2026-02-26 20:01:29', '2026-02-26 20:07:11', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(18, 'INV-20260226-F4A07', 7, 4, 'DRAFT', 'UNPAID', '9000.00', '0.00', '9000.00', '2026-02-26 20:21:21', '2026-02-26 20:21:32', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(19, 'INV-20260226-EE906', 7, 4, 'DRAFT', 'UNPAID', '3800.00', '0.00', '3800.00', '2026-02-26 20:27:08', '2026-03-22 10:55:06', '⏰ Tự động giải phóng tồn kho sau 15 phút không thanh toán', 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(20, 'INV-20260226-C848B', 7, 4, 'DRAFT', 'UNPAID', '2000.00', '0.00', '2000.00', '2026-02-26 20:31:21', '2026-03-22 10:55:06', '⏰ Tự động giải phóng tồn kho sau 15 phút không thanh toán', 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(21, 'INV-2026-001', NULL, 1, 'PAID', 'PAID', '250000.00', '0.00', '250000.00', '2026-03-03 21:28:32', '2026-03-03 21:28:32', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(22, 'INV-2026-002', NULL, 1, 'PAID', 'PAID', '480000.00', '20000.00', '460000.00', '2026-03-03 21:28:32', '2026-03-03 21:28:32', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(23, 'INV-2026-003', NULL, 1, 'PAID', 'PAID', '320000.00', '0.00', '320000.00', '2026-03-02 21:28:32', '2026-03-02 21:28:32', NULL, 'WHOLESALE', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(24, 'INV-2026-004', 1, 1, 'PAID', 'PAID', '750000.00', '50000.00', '700000.00', '2026-03-02 21:28:32', '2026-03-02 21:28:32', NULL, 'RETAIL', 'WEB', 1, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(25, 'INV-2026-005', NULL, 1, 'PAID', 'PAID', '190000.00', '0.00', '190000.00', '2026-03-01 21:28:32', '2026-03-01 21:28:32', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(26, 'INV-2026-006', NULL, 1, 'PAID', 'PAID', '960000.00', '60000.00', '900000.00', '2026-03-01 21:28:32', '2026-03-01 21:28:32', NULL, 'WHOLESALE', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(27, 'INV-2026-007', 2, 1, 'PAID', 'PAID', '380000.00', '0.00', '380000.00', '2026-02-28 21:28:32', '2026-02-28 21:28:32', NULL, 'RETAIL', 'WEB', 1, NULL, '30000.00', NULL, NULL, NULL, '0.00'),
(28, 'INV-2026-008', NULL, 1, 'PAID', 'PAID', '550000.00', '0.00', '550000.00', '2026-03-04 21:28:32', '2026-03-04 21:28:32', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(29, 'INV-2026-009', NULL, 1, 'PAID', 'PAID', '120000.00', '0.00', '120000.00', '2026-03-04 21:28:32', '2026-03-04 21:28:32', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(30, 'INV-2026-010', 3, 1, 'PAID', 'PAID', '1200000.00', '100000.00', '1100000.00', '2026-03-04 21:28:32', '2026-03-04 21:28:32', NULL, 'WHOLESALE', 'WEB', 0, NULL, '0.00', NULL, NULL, NULL, '50000.00'),
(31, 'INV-20260305-62CDC', 7, 4, 'PAID', 'PAID', '18000.00', '0.00', '18000.00', '2026-03-05 21:54:39', '2026-03-05 21:54:55', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(32, 'INV-20260305-D3D90', 8, 4, 'DRAFT', 'UNPAID', '211090.00', '0.00', '211090.00', '2026-03-05 21:55:39', '2026-03-05 21:55:45', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(33, 'INV-20260305-54D52', 8, 4, 'DRAFT', 'UNPAID', '0.00', '0.00', '0.00', '2026-03-05 21:56:37', '2026-03-05 21:56:37', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(34, 'INV-20260305-E9007', 7, 4, 'DRAFT', 'UNPAID', '30000.00', '0.00', '30000.00', '2026-03-05 22:01:12', '2026-03-05 22:01:37', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(35, 'INV-20260305-8B206', 8, 4, 'DRAFT', 'UNPAID', '0.00', '0.00', '0.00', '2026-03-05 22:13:36', '2026-03-05 22:13:53', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(36, 'INV-20260306-4411C', 8, 4, 'PAID', 'PAID', '2000.00', '0.00', '2000.00', '2026-03-06 20:18:37', '2026-03-06 20:18:48', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(37, 'INV-20260311-8B8C3', 8, 4, 'DRAFT', 'UNPAID', '2000.00', '0.00', '2000.00', '2026-03-11 22:05:55', '2026-03-11 22:05:58', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(38, 'INV-20260321-51FF3', 10, 4, 'DRAFT', 'UNPAID', '20000.00', '0.00', '20000.00', '2026-03-21 16:12:43', '2026-03-21 16:12:53', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(39, 'INV-20260322-DA26B', 9, 4, 'DRAFT', 'UNPAID', '3800.00', '0.00', '3800.00', '2026-03-22 09:56:00', '2026-03-22 10:55:06', '⏰ Tự động giải phóng tồn kho sau 15 phút không thanh toán', 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(40, 'INV-20260322-6C962', 10, 4, 'DRAFT', 'UNPAID', '4000.00', '0.00', '4000.00', '2026-03-22 09:59:36', '2026-03-22 10:00:28', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(41, 'INV-20260322-A235B', 10, 4, 'DRAFT', 'UNPAID', '2000.00', '0.00', '2000.00', '2026-03-22 10:06:36', '2026-03-22 10:55:06', '⏰ Tự động giải phóng tồn kho sau 15 phút không thanh toán', 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(42, 'INV-20260322-E0EB7', 9, 4, 'DRAFT', 'UNPAID', '0.00', '0.00', '0.00', '2026-03-22 10:11:45', '2026-03-22 10:11:45', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(43, 'INV-20260322-DA016', NULL, 4, 'DRAFT', 'UNPAID', '2000.00', '0.00', '2000.00', '2026-03-22 10:46:53', '2026-03-22 10:47:03', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(44, 'INV-20260322-E9460', 10, 4, 'DRAFT', 'UNPAID', '10000.00', '0.00', '10000.00', '2026-03-22 10:48:04', '2026-03-22 10:48:12', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00'),
(45, 'INV-20260322-F775E', 10, 4, 'PAID', 'PAID', '2000.00', '0.00', '2000.00', '2026-03-22 16:40:57', '2026-03-22 16:41:07', NULL, 'RETAIL', 'POS', 0, NULL, '0.00', NULL, NULL, NULL, '0.00');

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
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
(22, 20, 2, 1, '2000.00', '2000.00', 'paracetamol 500mg'),
(23, 21, 101, 5, '10000.00', '50000.00', 'Paracetamol 500mg'),
(24, 21, 107, 4, '50000.00', '200000.00', 'Cetirizine 10mg'),
(25, 22, 101, 8, '10000.00', '80000.00', 'Paracetamol 500mg'),
(26, 22, 106, 10, '40000.00', '400000.00', 'Omeprazole 20mg'),
(27, 23, 102, 16, '20000.00', '320000.00', 'Amoxicillin 250mg'),
(28, 24, 101, 20, '10000.00', '200000.00', 'Paracetamol 500mg'),
(29, 24, 103, 11, '50000.00', '550000.00', 'Vitamin C 1000mg'),
(30, 25, 107, 6, '50000.00', '300000.00', 'Cetirizine 10mg'),
(31, 26, 101, 30, '10000.00', '300000.00', 'Paracetamol 500mg'),
(32, 26, 106, 22, '30000.00', '660000.00', 'Omeprazole 20mg'),
(33, 27, 107, 5, '50000.00', '250000.00', 'Cetirizine 10mg'),
(34, 27, 102, 6, '21667.00', '130000.00', 'Amoxicillin 250mg'),
(35, 28, 101, 15, '10000.00', '150000.00', 'Paracetamol 500mg'),
(36, 28, 106, 10, '40000.00', '400000.00', 'Omeprazole 20mg'),
(37, 29, 103, 3, '40000.00', '120000.00', 'Vitamin C 1000mg'),
(38, 30, 101, 50, '10000.00', '500000.00', 'Paracetamol 500mg'),
(39, 30, 106, 25, '28000.00', '700000.00', 'Omeprazole 20mg'),
(40, 31, 1, 9, '2000.00', '18000.00', 'amoxicillin 501mg'),
(41, 32, 4, 95, '2222.00', '211090.00', '12'),
(42, 34, 1, 15, '2000.00', '30000.00', 'amoxicillin 501mg'),
(43, 36, 1, 1, '2000.00', '2000.00', 'amoxicillin 501mg'),
(44, 37, 1, 1, '2000.00', '2000.00', 'amoxicillin 501mg'),
(45, 38, 2, 10, '2000.00', '20000.00', 'paracetamol 500mg'),
(46, 39, 1, 1, '2000.00', '2000.00', 'amoxicillin 501mg'),
(48, 39, 3, 1, '1800.00', '1800.00', 'paracetamol 500 test updated'),
(49, 40, 2, 1, '2000.00', '2000.00', 'paracetamol 500mg'),
(50, 40, 2, 1, '2000.00', '2000.00', 'paracetamol 500mg'),
(51, 41, 1, 1, '2000.00', '2000.00', 'amoxicillin 501mg'),
(52, 43, 2, 1, '2000.00', '2000.00', 'paracetamol 500mg'),
(53, 44, 2, 5, '2000.00', '10000.00', 'paracetamol 500mg'),
(54, 45, 1, 1, '2000.00', '2000.00', 'amoxicillin 501mg');

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
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
(16, 40, 40, 9),
(17, 43, 40, 1),
(21, 54, 40, 1);

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
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `payments`
--

INSERT INTO `payments` (`id`, `invoice_id`, `amount`, `payment_method`, `transaction_id`, `status`, `paid_at`, `meta`, `notes`) VALUES
(3, 8, '2000.00', 'CASH', 'TX_DEMO_001', 'SUCCESS', '2025-12-23 20:11:29', NULL, NULL),
(4, 9, '16910.00', 'CASH', NULL, 'SUCCESS', '2025-12-25 09:08:56', NULL, NULL),
(5, 21, '250000.00', 'CASH', NULL, 'SUCCESS', '2026-03-04 21:28:32', NULL, NULL),
(6, 22, '460000.00', 'CASH', NULL, 'SUCCESS', '2026-03-04 21:28:32', NULL, NULL),
(7, 23, '320000.00', 'CASH', NULL, 'SUCCESS', '2026-03-04 21:28:32', NULL, NULL),
(8, 24, '700000.00', 'BANK_TRANSFER', NULL, 'SUCCESS', '2026-03-04 21:28:32', NULL, NULL),
(9, 25, '190000.00', 'CASH', NULL, 'SUCCESS', '2026-03-04 21:28:32', NULL, NULL),
(10, 26, '900000.00', 'CASH', NULL, 'SUCCESS', '2026-03-04 21:28:32', NULL, NULL),
(11, 27, '380000.00', 'BANK_TRANSFER', NULL, 'SUCCESS', '2026-03-04 21:28:32', NULL, NULL),
(12, 28, '550000.00', 'CASH', NULL, 'SUCCESS', '2026-03-04 21:28:32', NULL, NULL),
(13, 29, '120000.00', 'CASH', NULL, 'SUCCESS', '2026-03-04 21:28:32', NULL, NULL),
(14, 30, '1100000.00', 'CASH', NULL, 'SUCCESS', '2026-03-04 21:28:32', NULL, NULL),
(15, 31, '18000.00', 'TRANSFER', NULL, 'SUCCESS', '2026-03-05 21:54:55', NULL, NULL),
(16, 36, '2000.00', 'CASH', NULL, 'SUCCESS', '2026-03-06 20:18:48', NULL, NULL),
(17, 45, '2000.00', 'CARD', NULL, 'SUCCESS', '2026-03-22 16:41:07', NULL, NULL);

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `buyer_order_items`
--
ALTER TABLE `buyer_order_items`
  ADD CONSTRAINT `FK3j6cm5mxk3fhs4crd3ge8ldpy` FOREIGN KEY (`order_id`) REFERENCES `buyer_orders` (`id`);

--
-- Các ràng buộc cho bảng `cart_items`
--
ALTER TABLE `cart_items`
  ADD CONSTRAINT `FKpcttvuq4mxppo8sxggjtn5i2c` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`);

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
-- Các ràng buộc cho bảng `payments`
--
ALTER TABLE `payments`
  ADD CONSTRAINT `fk_pay_inv` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
