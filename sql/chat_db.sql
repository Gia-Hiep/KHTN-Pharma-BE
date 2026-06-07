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
-- Cơ sở dữ liệu: `chat_db`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `conversations`
--

DROP TABLE IF EXISTS `conversations`;
CREATE TABLE IF NOT EXISTS `conversations` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'USER_TO_ADMIN, PHARMACY_TO_PHARMACY, SUPPORT',
  `participant_1` bigint(20) NOT NULL,
  `participant_2` bigint(20) NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, ARCHIVED, CLOSED',
  `last_message_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_conv_p1` (`participant_1`),
  KEY `idx_conv_p2` (`participant_2`),
  KEY `idx_conv_type` (`type`),
  KEY `idx_conv_status` (`status`),
  KEY `idx_conv_last_msg` (`last_message_at`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `conversations`
--

INSERT INTO `conversations` (`id`, `type`, `participant_1`, `participant_2`, `title`, `status`, `last_message_at`, `created_at`, `updated_at`) VALUES
(1, 'USER_TO_ADMIN', 1, 2, 'Hỗ trợ đơn hàng #INV_DEMO_001', 'ACTIVE', '2026-02-08 21:42:24', '2026-02-08 21:42:24', '2026-02-08 21:42:24'),
(2, 'SUPPORT', 1, 3, 'Hỏi về cách sử dụng hệ thống', 'ACTIVE', '2026-02-08 21:42:24', '2026-02-08 21:42:24', '2026-02-08 21:42:24'),
(3, 'SUPPORT', 6, 4, 'Yêu cầu hỗ trợ', 'CLOSED', '2026-03-01 12:38:19', '2026-02-26 09:07:47', '2026-03-01 12:49:16'),
(4, 'SUPPORT', 6, 4, 'CẦN GIÚP VỀ THUỐC', 'CLOSED', '2026-02-26 09:11:23', '2026-02-26 09:08:08', '2026-03-01 12:49:18'),
(5, 'SUPPORT', 6, 4, 'DSA', 'CLOSED', NULL, '2026-02-26 09:08:26', '2026-03-01 12:49:20'),
(6, 'SUPPORT', 6, 4, 'ádsadas', 'CLOSED', '2026-02-26 09:11:19', '2026-02-26 09:11:17', '2026-03-01 12:49:17'),
(7, 'SUPPORT', 6, 4, 'đâs', 'CLOSED', '2026-03-01 12:49:08', '2026-03-01 12:46:58', '2026-03-01 12:49:13'),
(8, 'SUPPORT', 6, 4, 'sdad', 'CLOSED', '2026-03-06 13:27:20', '2026-03-06 13:26:51', '2026-03-07 08:47:52'),
(9, 'SUPPORT', 6, 4, 'dá', 'ACTIVE', NULL, '2026-03-16 14:15:11', '2026-03-22 02:40:03');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `messages`
--

DROP TABLE IF EXISTS `messages`;
CREATE TABLE IF NOT EXISTS `messages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint(20) NOT NULL,
  `sender_id` bigint(20) NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `message_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'TEXT' COMMENT 'TEXT, IMAGE, FILE, SYSTEM',
  `attachment_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `attachment_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `read_at` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_msg_conv` (`conversation_id`),
  KEY `idx_msg_sender` (`sender_id`),
  KEY `idx_msg_created` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `messages`
--

INSERT INTO `messages` (`id`, `conversation_id`, `sender_id`, `content`, `message_type`, `attachment_url`, `attachment_name`, `read_at`, `is_deleted`, `created_at`) VALUES
(1, 1, 1, 'Chào admin, tôi cần hỗ trợ về đơn hàng #INV_DEMO_001', 'TEXT', NULL, NULL, NULL, 0, '2026-02-08 21:42:24'),
(2, 1, 2, 'Chào bạn, tôi có thể giúp gì cho bạn?', 'TEXT', NULL, NULL, NULL, 0, '2026-02-08 21:42:24'),
(3, 1, 1, 'Đơn hàng của tôi đã giao chưa?', 'TEXT', NULL, NULL, NULL, 0, '2026-02-08 21:42:24'),
(4, 2, 1, 'Làm sao để xem lịch sử đơn hàng?', 'TEXT', NULL, NULL, NULL, 0, '2026-02-08 21:42:24'),
(5, 2, 3, 'Bạn vào menu Đơn hàng > Lịch sử để xem', 'TEXT', NULL, NULL, NULL, 0, '2026-02-08 21:42:24'),
(6, 6, 6, 'ádasd', 'TEXT', NULL, NULL, '2026-03-01 12:49:16', 0, '2026-02-26 09:11:19'),
(7, 4, 6, 'sadsadsa', 'TEXT', NULL, NULL, '2026-03-01 12:49:18', 0, '2026-02-26 09:11:23'),
(8, 3, 6, 'anhh hiep', 'TEXT', NULL, NULL, '2026-03-01 12:49:15', 0, '2026-02-26 09:11:28'),
(9, 3, 6, 'dsa', 'TEXT', NULL, NULL, '2026-03-01 12:49:15', 0, '2026-03-01 12:38:10'),
(10, 3, 6, 'dsa', 'TEXT', NULL, NULL, '2026-03-01 12:49:15', 0, '2026-03-01 12:38:19'),
(11, 7, 6, 'dsadsadsa', 'TEXT', NULL, NULL, '2026-03-01 12:49:04', 0, '2026-03-01 12:47:00'),
(12, 7, 4, 'sao ban', 'TEXT', NULL, NULL, '2026-03-01 12:49:38', 0, '2026-03-01 12:49:08'),
(13, 8, 6, 'hello', 'TEXT', NULL, NULL, '2026-03-06 13:27:02', 0, '2026-03-06 13:26:54'),
(14, 8, 4, 'sao bạn', 'TEXT', NULL, NULL, '2026-03-06 13:27:16', 0, '2026-03-06 13:27:07'),
(15, 8, 6, 'đấ', 'TEXT', NULL, NULL, '2026-03-06 13:27:30', 0, '2026-03-06 13:27:20');

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `messages`
--
ALTER TABLE `messages`
  ADD CONSTRAINT `fk_msg_conv` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
