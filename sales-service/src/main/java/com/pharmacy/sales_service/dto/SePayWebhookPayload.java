package com.pharmacy.sales_service.dto;

import lombok.Data;

/**
 * DTO nhận webhook từ SePay khi có biến động số dư ngân hàng.
 * Docs: https://sepay.vn
 */
@Data
public class SePayWebhookPayload {
    private Long id;                // SePay transaction ID
    private String gateway;         // Tên ngân hàng (e.g. "BIDV")
    private String transactionDate; // "2024-01-01 14:02:00"
    private String accountNumber;   // Số tài khoản nhận
    private String code;            // Mã thanh toán trích xuất bởi SePay (có thể null)
    private String content;         // Nội dung chuyển khoản đầy đủ
    private String transferType;    // "in" = tiền vào, "out" = tiền ra
    private Long transferAmount;    // Số tiền (VND)
    private Long accumulated;       // Số dư lũy kế
    private String subAccount;      // Sub-account (nếu có)
    private String referenceCode;   // Mã tham chiếu
    private String description;     // Mô tả đầy đủ từ SMS/bank
}
