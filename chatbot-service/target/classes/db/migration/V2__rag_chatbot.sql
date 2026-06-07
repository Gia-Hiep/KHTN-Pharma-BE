-- =============================================
-- RAG Chatbot Migration: 3 new tables
-- chatbot-service database
-- =============================================

-- 1. Documents: source knowledge (FAQ, POLICY, GUIDE)
CREATE TABLE IF NOT EXISTS bot_documents (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  source_type VARCHAR(30) NOT NULL COMMENT 'FAQ, POLICY, GUIDE',
  content MEDIUMTEXT NOT NULL,
  active BOOLEAN DEFAULT TRUE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Document chunks with embeddings (JSON)
CREATE TABLE IF NOT EXISTS bot_document_chunks (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  document_id BIGINT,
  chunk_index INT NOT NULL,
  content TEXT NOT NULL,
  embedding JSON COMMENT 'Gemini embedding vector [float array]',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (document_id) REFERENCES bot_documents(id) ON DELETE CASCADE,
  INDEX idx_chunk_doc (document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Query logs for evaluation + debugging
CREATE TABLE IF NOT EXISTS bot_query_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  session_id BIGINT,
  user_id BIGINT,
  message TEXT NOT NULL,
  detected_intent VARCHAR(30),
  route_used VARCHAR(20) COMMENT 'RAG, API, GUARDRAIL, OUT_OF_SCOPE',
  response_text TEXT,
  response_time_ms INT,
  blocked_by_guardrail BOOLEAN DEFAULT FALSE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_qlog_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- Seed FAQ/Policy documents for RAG
-- =============================================

INSERT INTO bot_documents (title, source_type, content) VALUES
('Chính sách giao hàng', 'POLICY',
'Chính sách giao hàng của Pharmacy Online:
- Đơn hàng sẽ được xử lý trong vòng 24 giờ sau khi đặt hàng thành công.
- Thời gian giao hàng: 2-5 ngày làm việc tùy khu vực.
- Phí ship: miễn phí cho đơn hàng trên 500.000đ, dưới 500.000đ phí ship 30.000đ.
- Khu vực nội thành: giao trong 1-2 ngày.
- Khu vực ngoại thành / tỉnh: giao trong 3-5 ngày.
- Đơn hàng có thể theo dõi qua mã đơn trên hệ thống.'),

('Chính sách đổi trả', 'POLICY',
'Chính sách đổi trả hàng:
- Thời hạn đổi trả: 7 ngày kể từ ngày nhận hàng.
- Điều kiện: sản phẩm còn nguyên seal, chưa sử dụng, còn hóa đơn.
- Thuốc đã mở seal không được đổi trả (theo quy định dược phẩm).
- Trường hợp hàng lỗi do nhà sản xuất: đổi trả miễn phí.
- Liên hệ hotline 1900-xxxx hoặc chat với dược sĩ để yêu cầu đổi trả.'),

('Chính sách thanh toán', 'POLICY',
'Phương thức thanh toán hỗ trợ:
- Thanh toán khi nhận hàng (COD).
- Chuyển khoản ngân hàng qua VietQR.
- Thanh toán online.
- Đối với khách hàng B2B: hỗ trợ công nợ theo thỏa thuận.
- Hóa đơn điện tử được gửi qua email sau khi đơn hàng hoàn thành.'),

('Hướng dẫn đặt hàng', 'GUIDE',
'Cách đặt hàng trên Pharmacy Online:
Bước 1: Đăng nhập tài khoản.
Bước 2: Tìm sản phẩm bằng thanh tìm kiếm hoặc duyệt theo danh mục.
Bước 3: Thêm sản phẩm vào giỏ hàng, chọn số lượng.
Bước 4: Vào giỏ hàng, kiểm tra và chọn Thanh toán.
Bước 5: Nhập địa chỉ giao hàng, chọn phương thức thanh toán.
Bước 6: Xác nhận đơn hàng.
Bước 7: Theo dõi đơn hàng qua mục Đơn hàng của tôi.'),

('Hướng dẫn tra cứu đơn hàng', 'GUIDE',
'Cách tra cứu đơn hàng:
- Đăng nhập vào hệ thống.
- Vào mục "Đơn hàng" trên menu.
- Xem danh sách đơn hàng với trạng thái: PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED.
- Nhấn vào mã đơn để xem chi tiết.
- Có thể hủy đơn nếu trạng thái còn PENDING.'),

('Giờ làm việc và liên hệ', 'FAQ',
'Thông tin liên hệ Pharmacy Online:
- Giờ mở cửa: 8:00 - 21:00 hàng ngày (kể cả Chủ Nhật).
- Hotline: 1900-xxxx (8:00 - 20:00).
- Email: support@pharmacy-online.vn
- Chat trực tuyến với dược sĩ: có sẵn trên website.
- Địa chỉ: 123 Đường ABC, Quận XYZ, TP.HCM.'),

('Giới thiệu website', 'FAQ',
'Giới thiệu về Pharmacy Online:
Pharmacy Online là hệ thống nhà thuốc trực tuyến B2B, chuyên cung cấp dược phẩm cho các nhà thuốc và cơ sở y tế.
Hệ thống hỗ trợ:
- Đặt hàng trực tuyến 24/7
- Tra cứu sản phẩm theo tên, nhóm bệnh, hoạt chất
- Theo dõi đơn hàng real-time
- Chat hỗ trợ với dược sĩ
- Quản lý công nợ B2B
- Chương trình loyalty cho khách hàng thân thiết
Website được xây dựng trên nền tảng công nghệ hiện đại, đảm bảo trải nghiệm mua hàng nhanh chóng và an toàn.'),

('Chương trình khách hàng thân thiết', 'POLICY',
'Chương trình loyalty Pharmacy Online:
- Tích điểm: mỗi 10.000đ mua hàng = 1 điểm loyalty.
- Quy đổi: 100 điểm = giảm 50.000đ cho đơn hàng tiếp theo.
- Hạng thành viên: Đồng (0-99 điểm), Bạc (100-499 điểm), Vàng (500+ điểm).
- Ưu đãi hạng Vàng: miễn phí ship, ưu tiên xử lý đơn, coupon riêng hàng tháng.
- Điểm loyalty có hiệu lực 12 tháng kể từ ngày tích.
- Xem điểm và hạng: đăng nhập > Tài khoản > Loyalty.');
