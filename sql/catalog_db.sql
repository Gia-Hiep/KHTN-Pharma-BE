-- phpMyAdmin SQL Dump
-- version 5.0.2
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1:3306
-- Thời gian đã tạo: Th5 04, 2026 lúc 01:44 PM
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
-- Cấu trúc bảng cho bảng `bot_catalog_chunks`
--

DROP TABLE IF EXISTS `bot_catalog_chunks`;
CREATE TABLE IF NOT EXISTS `bot_catalog_chunks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `source_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_id` bigint(20) NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `metadata_json` json DEFAULT NULL,
  `embedding` json DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_catalog_chunk_source` (`source_type`,`source_id`),
  KEY `idx_catalog_chunk_active` (`active`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `categories`
--

INSERT INTO `categories` (`id`, `code`, `name`, `description`) VALUES
(1, 'giam-dau', 'Thuốc giảm đau, hạ sốt', 'Các loại thuốc giảm đau, hạ sốt thông dụng'),
(2, 'khang-sinh', 'Thuốc kháng sinh', 'Thuốc kháng sinh điều trị nhiễm khuẩn'),
(3, 'ho-cam', 'Thuốc ho, cảm cúm', 'Thuốc điều trị triệu chứng ho, cảm, sổ mũi'),
(4, 'tieu-hoa', 'Thuốc tiêu hóa', 'Thuốc điều trị rối loạn tiêu hóa, dạ dày'),
(5, 'tim-mach', 'Thuốc tim mạch', 'Thuốc điều trị huyết áp, tim mạch'),
(6, 'vitamin', 'Vitamin & Thực phẩm chức năng', 'Vitamin, khoáng chất và thực phẩm bổ sung'),
(7, 'da-lieu', 'Thuốc da liễu', 'Thuốc bôi ngoài da, sát khuẩn'),
(8, 'mat-tmh', 'Thuốc mắt, tai mũi họng', 'Thuốc nhỏ mắt, tai mũi họng');

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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `disease_groups`
--

INSERT INTO `disease_groups` (`id`, `code`, `name`, `description`, `keywords`) VALUES
(1, 'dau-dau', 'Đau đầu, đau nửa đầu', 'Các triệu chứng đau đầu, đau nửa đầu, migraine', 'đau đầu,nhức đầu,migraine,đau nửa đầu'),
(2, 'cam-cum', 'Cảm cúm, sốt', 'Cảm cúm, sốt, viêm đường hô hấp trên', 'cảm cúm,sốt,cảm lạnh,sổ mũi,nghẹt mũi,hắt hơi'),
(3, 'nhiem-khuan', 'Nhiễm khuẩn, viêm', 'Nhiễm khuẩn, viêm do vi khuẩn', 'nhiễm khuẩn,viêm,nhiễm trùng,kháng sinh'),
(4, 'tieu-hoa-benh', 'Rối loạn tiêu hóa', 'Đau dạ dày, tiêu chảy, trào ngược, khó tiêu', 'đau bụng,tiêu chảy,đau dạ dày,trào ngược,khó tiêu'),
(5, 'huyet-ap', 'Huyết áp, tim mạch', 'Tăng huyết áp, bệnh tim mạch', 'huyết áp,cao huyết áp,tim mạch,tăng huyết áp'),
(6, 'da-dung', 'Chăm sóc da, sát khuẩn', 'Sát khuẩn vết thương, chăm sóc da', 'sát khuẩn,vết thương,bỏng,da liễu,ngứa');

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
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `medicines`
--

INSERT INTO `medicines` (`id`, `code`, `name`, `generic_name`, `unit`, `is_rx`, `manufacturer`, `category_id`, `default_supplier_id`, `sale_price`, `barcode`, `image_url`, `description`, `usage_instructions`, `side_effects`, `status`, `created_at`, `updated_at`, `active_ingredient`, `dosage_form`, `origin`, `package_size`) VALUES
(1, 'PARA500', 'Paracetamol 500mg', 'Paracetamol', 'Viên', 0, 'DHG Pharma', 1, 1, '2000.00', '8935049000011', '/uploads/medicines/52e171df-0c3e-4a6c-a3e1-93de3e66b274.jpeg', 'Thuốc giảm đau, hạ sốt thông dụng', 'Uống 1-2 viên mỗi 4-6 giờ khi cần', 'Hiếm gặp: dị ứng, phát ban', 'ACTIVE', '2026-04-13 21:10:34', '2026-04-20 18:37:20', 'Paracetamol 500mg', 'Viên nén', 'Việt Nam', 'Hộp 10 vỉ x 10 viên'),
(2, 'IBUP400', 'Ibuprofen 400mg', 'Ibuprofen', 'Viên', 0, 'Traphaco', 1, 2, '3500.00', '8935049000028', '', 'Thuốc giảm đau, kháng viêm không steroid', 'Uống 1 viên mỗi 6-8 giờ sau ăn', 'Có thể gây đau dạ dày, buồn nôn', 'ACTIVE', '2026-04-13 21:10:34', '2026-04-20 18:37:51', 'Ibuprofen 400mg', 'Viên nén bao phim', 'Việt Nam', 'Hộp 10 vỉ x 10 viên'),
(3, 'AMOX500', 'Amoxicillin 500mg', 'Amoxicillin', 'Viên', 1, 'DHG Pharma', 2, 1, '1000.00', '8935049000035', NULL, 'Thuốc kháng sinh nhóm penicillin', 'Uống theo chỉ định của bác sĩ', 'Có thể gây tiêu chảy, dị ứng', 'ACTIVE', '2026-04-13 21:10:34', '2026-04-20 18:43:36', 'Amoxicillin trihydrat 500mg', 'Viên nang cứng', 'Việt Nam', 'Hộp 10 vỉ x 10 viên'),
(4, 'AZIT250', 'Azithromycin 250mg', 'Azithromycin', 'Viên', 1, 'Pymepharco', 2, 3, '12000.00', '8935049000042', NULL, 'Thuốc kháng sinh nhóm macrolid', 'Uống theo chỉ định của bác sĩ', 'Có thể gây buồn nôn, đau bụng', 'ACTIVE', '2026-04-13 21:10:34', '2026-04-13 21:10:34', 'Azithromycin dihydrat 250mg', 'Viên nén bao phim', 'Việt Nam', 'Hộp 1 vỉ x 6 viên'),
(5, 'COLD', 'Coldacmin Forte', 'Paracetamol + Chlorpheniramine', 'Viên', 0, 'DHG Pharma', 3, 1, '4000.00', '8935049000059', NULL, 'Thuốc điều trị cảm cúm, sổ mũi, nghẹt mũi', 'Uống 1 viên mỗi 6 giờ khi có triệu chứng', 'Có thể gây buồn ngủ', 'ACTIVE', '2026-04-13 21:10:34', '2026-04-13 21:10:34', 'Paracetamol 500mg, Chlorpheniramine 2mg', 'Viên nén', 'Việt Nam', 'Hộp 25 vỉ x 4 viên'),
(6, 'OMEP20', 'Omeprazol 20mg', 'Omeprazol', 'Viên', 1, 'Traphaco', 4, 2, '4500.00', '8935049000066', NULL, 'Thuốc ức chế bơm proton, điều trị loét dạ dày', 'Uống trước ăn sáng 30 phút', 'Có thể gây đau đầu, buồn nôn', 'ACTIVE', '2026-04-13 21:10:34', '2026-04-13 21:10:34', 'Omeprazol 20mg', 'Viên nang cứng', 'Việt Nam', 'Hộp 3 vỉ x 10 viên'),
(7, 'SMECTA', 'Smecta 3g', 'Diosmectit', 'Gói', 0, 'Ipsen (Pháp)', 4, 2, '8000.00', '8935049000073', NULL, 'Thuốc điều trị tiêu chảy cấp và mạn tính', 'Pha 1 gói với nước, uống giữa các bữa ăn', 'Hiếm gặp: táo bón nhẹ', 'ACTIVE', '2026-04-13 21:10:34', '2026-04-13 21:10:34', 'Diosmectit 3g', 'Bột pha hỗn dịch', 'Pháp', 'Hộp 30 gói'),
(8, 'AMLO5', 'Amlodipin 5mg', 'Amlodipin', 'Viên', 1, 'Pymepharco', 5, 3, '2500.00', '8935049000080', NULL, 'Thuốc điều trị tăng huyết áp', 'Uống 1 viên/ngày theo chỉ định bác sĩ', 'Có thể gây phù chân, chóng mặt', 'ACTIVE', '2026-04-13 21:10:34', '2026-04-13 21:10:34', 'Amlodipin besilat 5mg', 'Viên nén', 'Việt Nam', 'Hộp 3 vỉ x 10 viên'),
(9, 'VITC', 'Vitamin C 1000mg', 'Acid ascorbic', 'Viên sủi', 0, 'DHG Pharma', 6, 1, '5000.00', '8935049000097', NULL, 'Bổ sung vitamin C, tăng cường sức đề kháng', 'Hòa tan 1 viên trong 200ml nước, uống 1 viên/ngày', 'Hiếm gặp ở liều thường', 'ACTIVE', '2026-04-13 21:10:34', '2026-04-13 21:10:34', 'Acid ascorbic 1000mg', 'Viên nén sủi bọt', 'Việt Nam', 'Tuýp 20 viên'),
(10, 'OMEGA3', 'Omega 3-6-9', 'DHA + EPA', 'Viên', 0, 'Traphaco', 6, 2, '12000.00', '8935049000103', NULL, 'Bổ sung Omega 3-6-9, hỗ trợ tim mạch, não bộ', 'Uống 1-2 viên/ngày sau ăn', 'Có thể gây ợ cá nhẹ', 'ACTIVE', '2026-04-13 21:10:34', '2026-04-13 21:10:34', 'DHA 120mg, EPA 180mg', 'Viên nang mềm', 'Việt Nam', 'Lọ 100 viên'),
(11, 'BETADINE', 'Betadine 10%', 'Povidone-iodine', 'Chai', 0, 'Mundipharma', 7, 3, '35000.00', '8935049000110', NULL, 'Dung dịch sát khuẩn vết thương, phòng nhiễm trùng', 'Bôi trực tiếp lên vết thương 1-2 lần/ngày', 'Hiếm gặp: kích ứng da', 'ACTIVE', '2026-04-13 21:10:34', '2026-04-13 21:10:34', 'Povidone-iodine 10%', 'Dung dịch bôi ngoài', 'Thái Lan', 'Chai 125ml'),
(12, 'NATRI09', 'Natri Clorid 0.9%', 'NaCl', 'Lọ', 0, 'DHG Pharma', 8, 1, '15000.00', '8935049000127', NULL, 'Dung dịch nhỏ mắt, rửa mũi', 'Nhỏ 1-2 giọt mỗi mắt khi cần', 'Không có tác dụng phụ đáng kể', 'ACTIVE', '2026-04-13 21:10:34', '2026-04-13 21:10:34', 'Natri clorid 0.9%', 'Dung dịch nhỏ mắt', 'Việt Nam', 'Lọ 10ml');

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
(1, 1),
(1, 2),
(2, 1),
(3, 3),
(4, 3),
(5, 2),
(6, 4),
(7, 4),
(8, 5),
(9, 2),
(10, 5),
(11, 3),
(11, 6),
(12, 6);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `medicine_images`
--

DROP TABLE IF EXISTS `medicine_images`;
CREATE TABLE IF NOT EXISTS `medicine_images` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_primary` bit(1) NOT NULL,
  `medicine_id` bigint(20) NOT NULL,
  `sort_order` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_mi_medicine_id` (`medicine_id`),
  KEY `idx_mi_medicine_sort` (`medicine_id`,`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `medicine_images`
--

INSERT INTO `medicine_images` (`id`, `created_at`, `file_name`, `image_url`, `is_primary`, `medicine_id`, `sort_order`) VALUES
(1, '2026-03-18 22:41:36.043421', '3ff015c2-386d-47ef-9213-83c0ba11db52.png', '/uploads/medicines/3ff015c2-386d-47ef-9213-83c0ba11db52.png', b'0', 4, 0),
(2, '2026-03-18 22:41:36.097080', '1f95434b-882a-4e13-9a30-b411bae1d27f.png', '/uploads/medicines/1f95434b-882a-4e13-9a30-b411bae1d27f.png', b'0', 4, 1),
(4, '2026-03-18 22:41:36.113097', '3c8c3aa4-861e-4465-b782-bd7be32a0cf5.png', '/uploads/medicines/3c8c3aa4-861e-4465-b782-bd7be32a0cf5.png', b'0', 4, 3),
(5, '2026-03-18 22:41:36.123371', '71810e5c-960d-485c-8524-693aa7ab796b.png', '/uploads/medicines/71810e5c-960d-485c-8524-693aa7ab796b.png', b'0', 4, 4),
(6, '2026-03-18 22:41:36.128256', '65538394-697c-4c60-868d-ad27abc27568.png', '/uploads/medicines/65538394-697c-4c60-868d-ad27abc27568.png', b'0', 4, 5),
(7, '2026-03-18 22:41:36.133111', '42311b23-f65b-4f8a-8b8f-960bd0aaac07.png', '/uploads/medicines/42311b23-f65b-4f8a-8b8f-960bd0aaac07.png', b'0', 4, 6),
(8, '2026-03-18 22:41:36.145504', 'c87c03a6-2fd2-4615-9246-080e0ac9d043.png', '/uploads/medicines/c87c03a6-2fd2-4615-9246-080e0ac9d043.png', b'0', 4, 7),
(9, '2026-03-18 22:47:41.378198', '287ebd6d-ee83-4e5d-b59a-8221eabd9979.png', '/uploads/medicines/287ebd6d-ee83-4e5d-b59a-8221eabd9979.png', b'1', 4, 8),
(10, '2026-03-18 22:47:43.964505', '7a2775ff-ca97-4201-b3eb-8d3950f82bae.png', '/uploads/medicines/7a2775ff-ca97-4201-b3eb-8d3950f82bae.png', b'0', 4, 9),
(11, '2026-03-22 09:38:40.053557', '61ead81e-b3ab-4ec4-9142-e9821343614a.png', '/uploads/medicines/61ead81e-b3ab-4ec4-9142-e9821343614a.png', b'0', 5, 0),
(12, '2026-03-22 09:38:53.564835', 'c77ceb5b-b178-44fe-9790-1c776cf82282.png', '/uploads/medicines/c77ceb5b-b178-44fe-9790-1c776cf82282.png', b'0', 5, 1),
(13, '2026-03-22 09:38:53.571966', 'f0a15706-0646-4559-9403-6cf98c22e1f0.png', '/uploads/medicines/f0a15706-0646-4559-9403-6cf98c22e1f0.png', b'0', 5, 2),
(14, '2026-03-22 09:38:53.578212', '8ff55b10-aa8a-4fd4-926b-11455cb95291.png', '/uploads/medicines/8ff55b10-aa8a-4fd4-926b-11455cb95291.png', b'0', 5, 3),
(15, '2026-03-22 09:38:53.584519', '189b3034-1a20-4fc0-a14f-8f011ce93cc9.png', '/uploads/medicines/189b3034-1a20-4fc0-a14f-8f011ce93cc9.png', b'1', 5, 4),
(16, '2026-03-22 09:38:53.590265', '147259a2-d219-4527-806f-23e8f6317e3b.png', '/uploads/medicines/147259a2-d219-4527-806f-23e8f6317e3b.png', b'0', 5, 5),
(17, '2026-03-22 09:38:53.595707', '8353d1d9-8d2e-4684-89c2-30ffbd7c4ea4.png', '/uploads/medicines/8353d1d9-8d2e-4684-89c2-30ffbd7c4ea4.png', b'0', 5, 6),
(18, '2026-03-22 09:38:53.600943', '592f9030-0e47-4506-8f30-76c40cec6c25.png', '/uploads/medicines/592f9030-0e47-4506-8f30-76c40cec6c25.png', b'0', 5, 7),
(19, '2026-03-22 09:43:28.874288', 'f76f5c8b-64b2-44ea-a72d-1083732f5b7f.png', '/uploads/medicines/f76f5c8b-64b2-44ea-a72d-1083732f5b7f.png', b'0', 2, 0),
(20, '2026-03-22 09:43:28.883439', 'acea2f4e-3897-4d1b-a613-d7bdb6558149.png', '/uploads/medicines/acea2f4e-3897-4d1b-a613-d7bdb6558149.png', b'1', 2, 1),
(21, '2026-03-22 09:43:28.889998', 'f4cfc825-3ff6-4b93-84fd-4b93054ebe26.png', '/uploads/medicines/f4cfc825-3ff6-4b93-84fd-4b93054ebe26.png', b'0', 2, 2),
(22, '2026-04-06 21:29:29.766943', '6c389e72-abec-4cf0-9e77-7f8bede67956.png', '/uploads/medicines/6c389e72-abec-4cf0-9e77-7f8bede67956.png', b'1', 6, 0),
(23, '2026-04-06 21:31:52.445767', '3b902428-1585-4fbc-bd56-ba0e08581fcd.png', '/uploads/medicines/3b902428-1585-4fbc-bd56-ba0e08581fcd.png', b'0', 6, 1),
(24, '2026-04-18 21:25:24.816160', '52e171df-0c3e-4a6c-a3e1-93de3e66b274.jpeg', '/uploads/medicines/52e171df-0c3e-4a6c-a3e1-93de3e66b274.jpeg', b'1', 1, 0);

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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `medicine_price_history`
--

INSERT INTO `medicine_price_history` (`id`, `medicine_id`, `old_price`, `new_price`, `changed_by`, `changed_at`) VALUES
(1, 3, '1200.00', '1500.00', 1, '2025-12-23 08:19:14'),
(2, 3, '1500.00', '1800.00', 1, '2025-12-23 08:19:18'),
(3, 1, '1000.00', '2000.00', 1, '2025-12-24 16:39:05'),
(4, 4, '2.00', '2222.00', 1, '2025-12-24 16:49:23'),
(5, 1, '2000.00', '20000.00', 4, '2026-04-18 21:21:50'),
(6, 1, '20000.00', '2000.00', 4, '2026-04-20 18:36:49'),
(7, 1, '2000.00', '20000.00', 4, '2026-04-20 18:37:01'),
(8, 1, '20000.00', '2000.00', 4, '2026-04-20 18:37:20'),
(9, 3, '5000.00', '1000.00', 4, '2026-04-20 18:43:36');

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
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `medicine_pricing_tiers`
--

INSERT INTO `medicine_pricing_tiers` (`id`, `medicine_id`, `tier_code`, `min_qty`, `price`, `discount_percent`, `effective_from`, `effective_to`, `created_at`, `created_by`) VALUES
(1, 1, 'RETAIL', 1, '2000.00', '0.00', '2026-01-01 00:00:00', NULL, '2026-04-13 21:10:34', 1),
(2, 2, 'RETAIL', 1, '3500.00', '0.00', '2026-01-01 00:00:00', NULL, '2026-04-13 21:10:34', 1),
(3, 3, 'RETAIL', 1, '5000.00', '0.00', '2026-01-01 00:00:00', NULL, '2026-04-13 21:10:34', 1),
(4, 4, 'RETAIL', 1, '12000.00', '0.00', '2026-01-01 00:00:00', NULL, '2026-04-13 21:10:34', 1),
(5, 5, 'RETAIL', 1, '4000.00', '0.00', '2026-01-01 00:00:00', NULL, '2026-04-13 21:10:34', 1),
(6, 6, 'RETAIL', 1, '4500.00', '0.00', '2026-01-01 00:00:00', NULL, '2026-04-13 21:10:34', 1),
(7, 7, 'RETAIL', 1, '8000.00', '0.00', '2026-01-01 00:00:00', NULL, '2026-04-13 21:10:34', 1),
(8, 8, 'RETAIL', 1, '2500.00', '0.00', '2026-01-01 00:00:00', NULL, '2026-04-13 21:10:34', 1),
(9, 9, 'RETAIL', 1, '5000.00', '0.00', '2026-01-01 00:00:00', NULL, '2026-04-13 21:10:34', 1),
(10, 10, 'RETAIL', 1, '12000.00', '0.00', '2026-01-01 00:00:00', NULL, '2026-04-13 21:10:34', 1),
(11, 11, 'RETAIL', 1, '35000.00', '0.00', '2026-01-01 00:00:00', NULL, '2026-04-13 21:10:34', 1),
(12, 12, 'RETAIL', 1, '15000.00', '0.00', '2026-01-01 00:00:00', NULL, '2026-04-13 21:10:34', 1);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `medicine_units`
--

DROP TABLE IF EXISTS `medicine_units`;
CREATE TABLE IF NOT EXISTS `medicine_units` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `medicine_id` bigint(20) NOT NULL,
  `unit_code` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `unit_label` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `conversion_factor` int(11) NOT NULL DEFAULT '1',
  `retail_price` decimal(15,2) NOT NULL,
  `wholesale_price` decimal(15,2) DEFAULT NULL,
  `wholesale_min_qty` int(11) DEFAULT NULL,
  `is_base_unit` tinyint(1) NOT NULL DEFAULT '0',
  `is_default_sale_unit` tinyint(1) NOT NULL DEFAULT '0',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_medicine_unit_code` (`medicine_id`,`unit_code`),
  KEY `idx_medicine_units_medicine` (`medicine_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `medicine_units`
--

INSERT INTO `medicine_units` (`id`, `medicine_id`, `unit_code`, `unit_label`, `conversion_factor`, `retail_price`, `wholesale_price`, `wholesale_min_qty`, `is_base_unit`, `is_default_sale_unit`, `is_active`, `created_at`, `updated_at`) VALUES
(2, 2, 'VIEN', 'Viên', 1, '3500.00', NULL, NULL, 1, 1, 1, '2026-04-18 20:10:55', '2026-04-20 21:09:58'),
(3, 3, 'BASE', 'Viên', 1, '5000.00', NULL, NULL, 0, 0, 0, '2026-04-18 20:10:55', '2026-04-20 18:43:43'),
(4, 4, 'VIEN', 'Viên', 1, '12000.00', '11000.00', 1000, 1, 1, 1, '2026-04-18 20:10:55', '2026-04-20 21:10:36'),
(5, 5, 'VIEN', 'Viên', 1, '4000.00', NULL, NULL, 1, 1, 1, '2026-04-18 20:10:55', '2026-04-20 21:09:58'),
(6, 6, 'VIEN', 'Viên', 1, '4500.00', NULL, NULL, 1, 1, 1, '2026-04-18 20:10:55', '2026-04-20 21:09:58'),
(7, 7, 'GOI', 'Gói', 1, '8000.00', NULL, NULL, 1, 1, 1, '2026-04-18 20:10:55', '2026-04-20 21:09:58'),
(8, 8, 'VIEN', 'Viên', 1, '2500.00', NULL, NULL, 1, 1, 1, '2026-04-18 20:10:55', '2026-04-20 21:09:58'),
(9, 9, 'VIEN_SUI', 'Viên sủi', 1, '5000.00', NULL, NULL, 1, 1, 1, '2026-04-18 20:10:55', '2026-04-20 21:09:58'),
(10, 10, 'VIEN', 'Viên', 1, '12000.00', NULL, NULL, 1, 1, 1, '2026-04-18 20:10:55', '2026-04-20 21:09:58'),
(11, 11, 'CHAI', 'Chai', 1, '35000.00', NULL, NULL, 1, 1, 1, '2026-04-18 20:10:55', '2026-04-20 21:09:58'),
(12, 12, 'LO', 'Lọ', 1, '15000.00', NULL, NULL, 1, 1, 1, '2026-04-18 20:10:55', '2026-04-20 21:09:58'),
(16, 1, 'HOP', 'Hộp', 1, '20000.00', '15000.00', 5, 0, 0, 1, '2026-04-18 21:21:50', '2026-04-20 18:37:20'),
(18, 1, 'VI', 'Vỉ', 6, '15000.00', '10000.00', 20, 0, 0, 1, '2026-04-20 18:11:22', '2026-04-20 18:11:22'),
(20, 1, 'THUNG', 'THÙNG', 1000, '1000000.00', '200000.00', 5, 0, 0, 1, '2026-04-20 18:18:27', '2026-04-20 18:18:27'),
(21, 1, 'VIEN', 'Viên', 1, '2000.00', '1500.00', 100, 1, 1, 1, '2026-04-20 18:18:48', '2026-04-20 21:09:57'),
(23, 3, 'VIEN', 'Viên', 1, '1000.00', '700.00', 100, 1, 1, 1, '2026-04-20 18:43:36', '2026-04-20 18:43:36');

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
