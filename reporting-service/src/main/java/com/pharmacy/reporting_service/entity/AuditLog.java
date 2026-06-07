package com.pharmacy.reporting_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Audit log theo dõi hành động quan trọng trong hệ thống
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Service tạo log: sales-service, catalog-service, v.v. */
    @Column(name = "service_name", nullable = false, length = 50)
    private String serviceName;

    /** Action: CREATE, UPDATE, DELETE, LOGIN, LOGOUT */
    @Column(nullable = false, length = 50)
    private String action;

    /** Entity bị tác động: Invoice, Medicine, Customer, v.v. */
    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_email", length = 100)
    private String userEmail;

    /** Nội dung thay đổi (JSON) */
    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;



    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
