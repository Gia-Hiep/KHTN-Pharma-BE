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
-- Cơ sở dữ liệu: `chatbot_db`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `bot_faqs`
--

DROP TABLE IF EXISTS `bot_faqs`;
CREATE TABLE IF NOT EXISTS `bot_faqs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `intent` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'MEDICINE_SEARCH, ORDER_STATUS, STORE_INFO, SIDE_EFFECTS, DOSAGE, GENERAL',
  `keywords` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Comma-separated trigger keywords',
  `question` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `answer` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `priority` int(11) NOT NULL DEFAULT '5',
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_faq_intent` (`intent`),
  KEY `idx_faq_active` (`active`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `bot_faqs`
--

INSERT INTO `bot_faqs` (`id`, `intent`, `keywords`, `question`, `answer`, `priority`, `active`, `created_at`, `updated_at`) VALUES
(1, 'STORE_INFO', 'giờ mở cửa,mấy giờ mở,khi nào mở,thời gian hoạt động,working hours', 'Nhà thuốc mở cửa mấy giờ?', '🏪 **Giờ hoạt động:**\n- Thứ 2 - Thứ 6: 7:00 - 21:00\n- Thứ 7: 7:00 - 20:00\n- Chủ nhật: 8:00 - 18:00\n\nBạn có thể đặt hàng online 24/7 qua website!', 8, 1, '2026-02-26 10:00:31', '2026-02-26 10:00:31'),
(2, 'STORE_INFO', 'địa chỉ,ở đâu,chỗ nào,location,địa điểm', 'Nhà thuốc ở đâu?', '📍 **Địa chỉ:** 123 Nguyễn Văn Linh, Quận 7, TP.HCM\n\n🚗 Bãi đậu xe miễn phí\n📞 Hotline: 1800-xxxx\n\nBạn cũng có thể đặt thuốc online và nhận giao tận nhà!', 7, 1, '2026-02-26 10:00:31', '2026-02-26 10:00:31'),
(3, 'STORE_INFO', 'hotline,số điện thoại,liên hệ,contact,gọi điện', 'Số điện thoại liên hệ?', '📞 **Hotline:** 1800-xxxx (miễn phí)\n✉️ **Email:** pharmacy@example.com\n💬 **Zalo:** 09xx-xxx-xxx\n\nChúng tôi luôn sẵn sàng hỗ trợ bạn!', 6, 1, '2026-02-26 10:00:31', '2026-02-26 10:00:31'),
(4, 'MEDICINE_SEARCH', 'tìm thuốc,tìm kiếm thuốc,mua thuốc,thuốc nào,loại thuốc', 'Làm thế nào để tìm thuốc?', '🔍 Bạn có thể tìm thuốc bằng cách:\n1. Nhập tên thuốc vào ô tìm kiếm\n2. Hoặc cho mình biết bạn đang cần điều trị bệnh gì, mình sẽ gợi ý thuốc phù hợp!\n\nBạn cần tìm thuốc gì vậy?', 9, 1, '2026-02-26 10:00:31', '2026-02-26 10:00:31'),
(5, 'MEDICINE_SEARCH', 'đau đầu,nhức đầu,headache,đầu đau,đau nửa đầu', 'Thuốc đau đầu?', '💊 **Gợi ý thuốc đau đầu thông thường:**\n- **Paracetamol 500mg** - Giảm đau, hạ sốt\n- **Ibuprofen 400mg** - Chống viêm, giảm đau\n- **Aspirin** - Giảm đau nhẹ\n\n⚠️ *Lưu ý: Nếu đau đầu kéo dài hoặc dữ dội, hãy gặp bác sĩ!*\n\nBạn muốn xem chi tiết loại thuốc nào?', 9, 1, '2026-02-26 10:00:31', '2026-02-26 10:00:31'),
(6, 'MEDICINE_SEARCH', 'cảm,sổ mũi,ngạt mũi,ho,đau họng,cúm,flu', 'Thuốc cảm cúm?', '🤧 **Gợi ý cho cảm cúm thông thường:**\n- **Paracetamol** - Giảm sốt, đau nhức\n- **Cetirizine** - Sổ mũi, nghẹt mũi\n- **Dextromethorphan** - Ho\n- **Vitamin C** - Tăng đề kháng\n\n💧 Nhớ uống nhiều nước và nghỉ ngơi đầy đủ nhé!', 9, 1, '2026-02-26 10:00:31', '2026-02-26 10:00:31'),
(7, 'MEDICINE_SEARCH', 'đau bụng,tiêu chảy,táo bón,khó tiêu,đầy bụng,dạ dày', 'Thuốc đau bụng?', '🏥 **Gợi ý thuốc tiêu hóa:**\n- **Buscopan** - Đau bụng co thắt\n- **Smecta** - Tiêu chảy, viêm ruột\n- **Oresol** - Bù nước khi tiêu chảy\n- **Omeprazole** - Đau dạ dày, trào ngược\n\n⚠️ Nếu đau bụng kéo dài, hãy đến gặp bác sĩ!', 8, 1, '2026-02-26 10:00:31', '2026-02-26 10:00:31'),
(8, 'SIDE_EFFECTS', 'tác dụng phụ,side effect,phản ứng phụ,có hại,nguy hiểm không', 'Thuốc có tác dụng phụ gì?', '⚠️ Mỗi loại thuốc có thể có các tác dụng phụ khác nhau.\n\nVui lòng cho mình biết tên thuốc cụ thể để được tư vấn chi tiết. Hoặc bạn có thể:\n1. Đọc tờ hướng dẫn sử dụng kèm theo\n2. Hỏi dược sĩ tại quầy\n3. Gọi hotline: 1800-xxxx', 8, 1, '2026-02-26 10:00:31', '2026-02-26 10:00:31'),
(9, 'DOSAGE', 'liều lượng,uống bao nhiêu,dùng mấy viên,cách dùng,hướng dẫn sử dụng', 'Liều lượng thuốc như thế nào?', '📋 Liều lượng phụ thuộc vào từng loại thuốc và từng người bệnh.\n\nBạn vui lòng cho mình biết tên thuốc cụ thể nhé! Hoặc liên hệ:\n- **Dược sĩ tư vấn:** 1800-xxxx\n- **Xem tờ hướng dẫn** đính kèm thuốc', 7, 1, '2026-02-26 10:00:31', '2026-02-26 10:00:31'),
(10, 'ORDER_STATUS', 'đơn hàng,order,tình trạng đơn,theo dõi đơn,giao hàng,shipping', 'Kiểm tra đơn hàng?', '📦 Để kiểm tra đơn hàng, bạn có thể:\n1. Đăng nhập vào tài khoản → Đơn hàng của tôi\n2. Sử dụng mã đơn hàng để tra cứu\n3. Gọi hotline: 1800-xxxx\n\nBạn đã có mã đơn hàng chưa?', 7, 1, '2026-02-26 10:00:31', '2026-02-26 10:00:31'),
(11, 'PRESCRIPTION', 'thuốc kê đơn,cần toa thuốc,đơn thuốc,bác sĩ kê,prescription,toa bác sĩ', 'Mua thuốc kê đơn thế nào?', '📝 **Quy trình mua thuốc kê đơn:**\n1. Mang toa thuốc gốc của bác sĩ\n2. Đến quầy dược và xuất trình toa\n3. Dược sĩ sẽ kiểm tra và cấp phát thuốc\n\n⚠️ *Chúng tôi không bán thuốc kê đơn khi không có toa bác sĩ!*', 9, 1, '2026-02-26 10:00:31', '2026-02-26 10:00:31'),
(12, 'PRICE', 'giá,bao nhiêu tiền,giá cả,chi phí,price,cost', 'Giá thuốc bao nhiêu?', '💰 Giá thuốc phụ thuộc vào từng loại. Bạn có thể:\n1. Tìm kiếm thuốc trên web để xem giá\n2. Hỏi trực tiếp tại quầy\n3. Chat với mình và cho biết tên thuốc cụ thể!\n\nChúng tôi cam kết giá cạnh tranh và có nhiều khuyến mãi hàng tuần! 🎁', 6, 1, '2026-02-26 10:00:31', '2026-02-26 10:00:31'),
(13, 'GREETING', 'xin chào,hello,hi,chào bạn,hey,chào buổi sáng,chào buổi chiều,good morning', 'Chào hỏi', '👋 Xin chào! Mình là trợ lý ảo của nhà thuốc.\n\nMình có thể giúp bạn:\n• 💊 Tìm kiếm thuốc\n• 📋 Thông tin về liều dùng\n• ⚠️ Tác dụng phụ\n• 📦 Theo dõi đơn hàng\n• 🏪 Thông tin nhà thuốc\n\nBạn cần hỗ trợ gì hôm nay?', 10, 1, '2026-02-26 10:00:31', '2026-02-26 10:00:31'),
(14, 'GREETING', 'cảm ơn,thank you,thanks,cám ơn', 'Cảm ơn', '😊 Không có gì! Mình rất vui được giúp bạn.\n\nNếu cần hỗ trợ thêm, bạn cứ hỏi mình nhé! Chúc bạn sức khỏe! 💪', 8, 1, '2026-02-26 10:00:31', '2026-02-26 10:00:31');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `bot_feedbacks`
--

DROP TABLE IF EXISTS `bot_feedbacks`;
CREATE TABLE IF NOT EXISTS `bot_feedbacks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `comment` text,
  `created_at` datetime(6) NOT NULL,
  `rating` int(11) NOT NULL,
  `resolved` bit(1) DEFAULT NULL,
  `session_id` bigint(20) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `bot_messages`
--

DROP TABLE IF EXISTS `bot_messages`;
CREATE TABLE IF NOT EXISTS `bot_messages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `session_id` bigint(20) NOT NULL,
  `role` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'USER or BOT',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `intent` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `confidence` double DEFAULT NULL,
  `faq_id` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_msg_session` (`session_id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `bot_messages`
--

INSERT INTO `bot_messages` (`id`, `session_id`, `role`, `content`, `intent`, `confidence`, `faq_id`, `created_at`) VALUES
(1, 1, 'USER', 'tôi muốn hỏi', 'FALLBACK', 0, NULL, '2026-02-26 08:35:05'),
(2, 1, 'BOT', 'Xin lỗi, mình chưa hiểu câu hỏi của bạn. 😅 Bạn có thể diễn đạt lại không?\n\nBạn có thể hỏi về:\n• Tìm kiếm thuốc\n• Tác dụng phụ\n• Cách dùng thuốc\n• Giờ mở cửa', 'FALLBACK', 0, NULL, '2026-02-26 08:35:05'),
(3, 1, 'USER', 'giờ mở cửa', 'STORE_INFO', 0.36000000000000004, 1, '2026-02-26 08:35:12'),
(4, 1, 'BOT', '🏪 **Giờ hoạt động:**\n- Thứ 2 - Thứ 6: 7:00 - 21:00\n- Thứ 7: 7:00 - 20:00\n- Chủ nhật: 8:00 - 18:00\n\nBạn có thể đặt hàng online 24/7 qua website!', 'STORE_INFO', 0.36000000000000004, NULL, '2026-02-26 08:35:12'),
(5, 1, 'USER', 'tác dụng phụ của thuốc', 'SIDE_EFFECTS', 0.36000000000000004, 8, '2026-02-26 08:35:26'),
(6, 1, 'BOT', '⚠️ Mỗi loại thuốc có thể có các tác dụng phụ khác nhau.\n\nVui lòng cho mình biết tên thuốc cụ thể để được tư vấn chi tiết. Hoặc bạn có thể:\n1. Đọc tờ hướng dẫn sử dụng kèm theo\n2. Hỏi dược sĩ tại quầy\n3. Gọi hotline: 1800-xxxx', 'SIDE_EFFECTS', 0.36000000000000004, NULL, '2026-02-26 08:35:26'),
(7, 1, 'USER', 'good morning', 'GREETING', 0.325, 13, '2026-02-26 08:35:48'),
(8, 1, 'BOT', '👋 Xin chào! Mình là trợ lý ảo của nhà thuốc.\n\nMình có thể giúp bạn:\n• 💊 Tìm kiếm thuốc\n• 📋 Thông tin về liều dùng\n• ⚠️ Tác dụng phụ\n• 📦 Theo dõi đơn hàng\n• 🏪 Thông tin nhà thuốc\n\nBạn cần hỗ trợ gì hôm nay?', 'GREETING', 0.325, NULL, '2026-02-26 08:35:48'),
(9, 1, 'USER', 'ko cần gì hết', 'FALLBACK', 0, NULL, '2026-02-26 08:35:57'),
(10, 1, 'BOT', 'Xin lỗi, mình chưa hiểu câu hỏi của bạn. 😅 Bạn có thể diễn đạt lại không?\n\nBạn có thể hỏi về:\n• Tìm kiếm thuốc\n• Tác dụng phụ\n• Cách dùng thuốc\n• Giờ mở cửa', 'FALLBACK', 0, NULL, '2026-02-26 08:35:57'),
(11, 1, 'USER', 'sdsad', 'FALLBACK', 0, NULL, '2026-02-26 08:36:00'),
(12, 1, 'BOT', 'Mình vẫn chưa hiểu rõ ý bạn. 🤔 Hãy thử hỏi cụ thể hơn nhé!', 'FALLBACK', 0, NULL, '2026-02-26 08:36:00'),
(13, 1, 'USER', 'dsadsa', 'FALLBACK', 0, NULL, '2026-02-26 08:36:02'),
(14, 1, 'BOT', 'Xin lỗi vì sự bất tiện! 🙏 Mình sẽ kết nối bạn với nhân viên tư vấn ngay nhé. Vui lòng chờ...', 'FALLBACK', 0, NULL, '2026-02-26 08:36:02'),
(15, 2, 'USER', 'đasad', 'FALLBACK', 0, NULL, '2026-02-26 08:36:05'),
(16, 2, 'BOT', 'Xin lỗi, mình chưa hiểu câu hỏi của bạn. 😅 Bạn có thể diễn đạt lại không?\n\nBạn có thể hỏi về:\n• Tìm kiếm thuốc\n• Tác dụng phụ\n• Cách dùng thuốc\n• Giờ mở cửa', 'FALLBACK', 0, NULL, '2026-02-26 08:36:05'),
(17, 2, 'USER', 'dsadsa', 'FALLBACK', 0, NULL, '2026-02-26 08:36:07'),
(18, 2, 'BOT', 'Mình vẫn chưa hiểu rõ ý bạn. 🤔 Hãy thử hỏi cụ thể hơn nhé!', 'FALLBACK', 0, NULL, '2026-02-26 08:36:07'),
(19, 2, 'USER', 'sda', 'FALLBACK', 0, NULL, '2026-02-26 08:36:07'),
(20, 2, 'BOT', 'Xin lỗi vì sự bất tiện! 🙏 Mình sẽ kết nối bạn với nhân viên tư vấn ngay nhé. Vui lòng chờ...', 'FALLBACK', 0, NULL, '2026-02-26 08:36:07'),
(21, 3, 'USER', 'sda', 'FALLBACK', 0, NULL, '2026-02-26 08:36:08'),
(22, 3, 'BOT', 'Xin lỗi, mình chưa hiểu câu hỏi của bạn. 😅 Bạn có thể diễn đạt lại không?\n\nBạn có thể hỏi về:\n• Tìm kiếm thuốc\n• Tác dụng phụ\n• Cách dùng thuốc\n• Giờ mở cửa', 'FALLBACK', 0, NULL, '2026-02-26 08:36:08'),
(23, 3, 'USER', 'ads', 'FALLBACK', 0, NULL, '2026-02-26 08:36:08'),
(24, 3, 'BOT', 'Mình vẫn chưa hiểu rõ ý bạn. 🤔 Hãy thử hỏi cụ thể hơn nhé!', 'FALLBACK', 0, NULL, '2026-02-26 08:36:08'),
(25, 3, 'USER', 'ads', 'FALLBACK', 0, NULL, '2026-02-26 08:36:09'),
(26, 3, 'BOT', 'Xin lỗi vì sự bất tiện! 🙏 Mình sẽ kết nối bạn với nhân viên tư vấn ngay nhé. Vui lòng chờ...', 'FALLBACK', 0, NULL, '2026-02-26 08:36:09'),
(27, 4, 'USER', 'ads', 'FALLBACK', 0, NULL, '2026-02-26 08:36:09'),
(28, 4, 'BOT', 'Xin lỗi, mình chưa hiểu câu hỏi của bạn. 😅 Bạn có thể diễn đạt lại không?\n\nBạn có thể hỏi về:\n• Tìm kiếm thuốc\n• Tác dụng phụ\n• Cách dùng thuốc\n• Giờ mở cửa', 'FALLBACK', 0, NULL, '2026-02-26 08:36:09'),
(29, 4, 'USER', 's', 'FALLBACK', 0, NULL, '2026-02-26 08:36:10'),
(30, 4, 'BOT', 'Mình vẫn chưa hiểu rõ ý bạn. 🤔 Hãy thử hỏi cụ thể hơn nhé!', 'FALLBACK', 0, NULL, '2026-02-26 08:36:10'),
(31, 4, 'USER', 'dsa', 'FALLBACK', 0, NULL, '2026-02-28 03:24:12'),
(32, 4, 'BOT', 'Xin lỗi vì sự bất tiện! 🙏 Mình sẽ kết nối bạn với nhân viên tư vấn ngay nhé. Vui lòng chờ...', 'FALLBACK', 0, NULL, '2026-02-28 03:24:12'),
(33, 5, 'USER', 'asdsasadsa', 'FALLBACK', 0, NULL, '2026-02-28 03:24:17'),
(34, 5, 'BOT', 'Xin lỗi, mình chưa hiểu câu hỏi của bạn. 😅 Bạn có thể diễn đạt lại không?\n\nBạn có thể hỏi về:\n• Tìm kiếm thuốc\n• Tác dụng phụ\n• Cách dùng thuốc\n• Giờ mở cửa', 'FALLBACK', 0, NULL, '2026-02-28 03:24:17');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `bot_sessions`
--

DROP TABLE IF EXISTS `bot_sessions`;
CREATE TABLE IF NOT EXISTS `bot_sessions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `session_token` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, ENDED, ESCALATED',
  `current_intent` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fallback_count` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_active_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_session_user` (`user_id`),
  KEY `idx_session_token` (`session_token`),
  KEY `idx_session_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `bot_sessions`
--

INSERT INTO `bot_sessions` (`id`, `user_id`, `session_token`, `status`, `current_intent`, `fallback_count`, `created_at`, `last_active_at`) VALUES
(1, 6, '49a2cd49-971c-4cd0-94a9-0e32a67e443a', 'ESCALATED', 'GREETING', 3, '2026-02-26 08:35:05', '2026-02-26 08:36:02'),
(2, 6, '77edb77e-6264-44c9-9de3-76484e375ffe', 'ESCALATED', NULL, 3, '2026-02-26 08:36:05', '2026-02-26 08:36:07'),
(3, 6, '13f4e9d1-da8b-4827-af2f-66da81026e2c', 'ESCALATED', NULL, 3, '2026-02-26 08:36:08', '2026-02-26 08:36:09'),
(4, 6, 'fa8ec298-f911-4247-b40c-bd9f5b032dec', 'ESCALATED', NULL, 3, '2026-02-26 08:36:09', '2026-02-28 03:24:12'),
(5, 6, '4c4f8a7c-7ca2-4a55-8ca9-201da18c711d', 'ACTIVE', NULL, 1, '2026-02-28 03:24:17', '2026-02-28 03:24:17');

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `bot_messages`
--
ALTER TABLE `bot_messages`
  ADD CONSTRAINT `fk_msg_session` FOREIGN KEY (`session_id`) REFERENCES `bot_sessions` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
