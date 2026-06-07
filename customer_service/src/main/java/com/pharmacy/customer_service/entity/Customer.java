package com.pharmacy.customer_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="customers",
        indexes = {
                @Index(name="idx_customer_phone", columnList="phone", unique = true)
        }
)
@Getter @Setter
public class Customer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    private String email;
    private String address;

    private LocalDate dateOfBirth;
    private String gender; // MALE/FEMALE/OTHER

    @Column(nullable = false)
    private int loyaltyPoints;

    private String notes;

    /**
     * Link to auth-service userId — allows buyers to access their own customer record.
     * Nullable (legacy customers may not have a linked auth user).
     */
    @Column(unique = true)
    private Long userId;

    // ===== B2B Fields (NEW) =====

    /**
     * Loại khách hàng: INDIVIDUAL (khách lẻ) hoặc PHARMACY (nhà thuốc bán sỉ)
     */
    @Column(name = "customer_type", nullable = false, length = 20)
    private String customerType = "INDIVIDUAL";

    /**
     * Tier khách hàng: REGULAR, SILVER, GOLD, VIP
     */
    @Column(length = 20)
    private String tier = "REGULAR";



    /**
     * Hạn mức tín dụng (công nợ tối đa được phép)
     */
    @Column(name = "credit_limit", precision = 15, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    /**
     * Công nợ hiện tại
     */
    @Column(name = "current_debt", precision = 15, scale = 2)
    private BigDecimal currentDebt = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (customerType == null) customerType = "INDIVIDUAL";
        if (tier == null) tier = "REGULAR";
        if (creditLimit == null) creditLimit = BigDecimal.ZERO;
        if (currentDebt == null) currentDebt = BigDecimal.ZERO;
    }
}
