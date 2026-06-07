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
-- Cơ sở dữ liệu: `notification_db`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `notifications`
--

DROP TABLE IF EXISTS `notifications`;
CREATE TABLE IF NOT EXISTS `notifications` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `message` text,
  `read_at` datetime(6) DEFAULT NULL,
  `reference_id` bigint(20) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `type` varchar(50) DEFAULT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notif_user_created` (`user_id`,`created_at`),
  KEY `idx_notif_user_read` (`user_id`,`read_at`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

--
-- Đang đổ dữ liệu cho bảng `notifications`
--

INSERT INTO `notifications` (`id`, `created_at`, `message`, `read_at`, `reference_id`, `title`, `type`, `user_id`) VALUES
(1, '2026-03-21 09:19:56.073884', 'Lý do: ád', '2026-03-21 09:20:08.451252', 28, 'Đơn hàng #28 bị từ chối', 'ORDER_STATUS', 6),
(2, '2026-03-21 09:21:17.932573', 'Đơn hàng của bạn đã được duyệt và sẽ sớm được xử lý.', '2026-03-21 09:21:36.487364', 27, 'Đơn hàng #27 đã được duyệt', 'ORDER_STATUS', 6),
(3, '2026-03-21 09:22:09.629546', 'Đơn hàng của bạn đã được duyệt và sẽ sớm được xử lý.', '2026-03-21 09:22:58.521166', 29, 'Đơn hàng #29 đã được duyệt', 'ORDER_STATUS', 6),
(4, '2026-03-21 09:22:32.342137', 'Đơn hàng đã được giao cho GIAO_HANG_NHANH.', '2026-03-21 09:22:55.583684', 29, 'Đơn hàng #29 đang giao', 'ORDER_STATUS', 6),
(5, '2026-03-21 09:22:33.706592', 'Đơn hàng đã được giao. Vui lòng xác nhận nhận hàng.', '2026-03-21 09:22:52.621675', 29, 'Đơn hàng #29 đã giao thành công', 'ORDER_STATUS', 6),
(6, '2026-03-28 13:26:26.441565', 'Đơn hàng của bạn đã được duyệt và sẽ sớm được xử lý.', '2026-03-30 12:28:24.173912', 31, 'Đơn hàng #31 đã được duyệt', 'ORDER_STATUS', 6),
(7, '2026-03-28 13:26:45.700289', 'Đơn hàng đã được giao cho LIEN_TINH.', '2026-03-28 13:27:29.929529', 31, 'Đơn hàng #31 đang giao', 'ORDER_STATUS', 6),
(8, '2026-03-28 13:26:48.777422', 'Đơn hàng đã được giao. Vui lòng xác nhận nhận hàng.', '2026-03-28 13:27:00.409690', 31, 'Đơn hàng #31 đã giao thành công', 'ORDER_STATUS', 6),
(9, '2026-03-30 15:10:18.238790', 'Đơn hàng của bạn đã được duyệt và sẽ sớm được xử lý.', '2026-03-30 15:10:34.504199', 34, 'Đơn hàng #34 đã được duyệt', 'ORDER_STATUS', 6),
(10, '2026-04-06 14:32:49.074812', 'Đơn hàng của bạn đã được duyệt và sẽ sớm được xử lý.', '2026-04-06 14:33:22.032452', 35, 'Đơn hàng #35 đã được duyệt', 'ORDER_STATUS', 6);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
