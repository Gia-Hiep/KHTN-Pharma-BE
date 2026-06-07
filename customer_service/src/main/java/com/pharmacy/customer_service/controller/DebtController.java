package com.pharmacy.customer_service.controller;

import com.pharmacy.customer_service.dto.CreateDebtRequest;
import com.pharmacy.customer_service.dto.PayDebtRequest;
import com.pharmacy.customer_service.entity.CustomerDebt;
import com.pharmacy.customer_service.service.DebtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/customers/debts")
@RequiredArgsConstructor
public class DebtController {

    private final DebtService service;

    /**
     * Lấy danh sách công nợ của customer
     */
    @GetMapping("/customer/{customerId}")
    public List<CustomerDebt> getDebts(@PathVariable Long customerId) {
        return service.getCustomerDebts(customerId);
    }

    /**
     * Lấy tổng nợ còn lại của customer
     */
    @GetMapping("/customer/{customerId}/total")
    public BigDecimal getTotalDebt(@PathVariable Long customerId) {
        return service.getTotalRemainingDebt(customerId);
    }

    /**
     * Tạo công nợ mới (khi khách sỉ mua trả sau)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public CustomerDebt create(@Valid @RequestBody CreateDebtRequest req) {
        return service.createDebt(req);
    }

    /**
     * Thanh toán công nợ
     */
    @PostMapping("/pay")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public CustomerDebt pay(@Valid @RequestBody PayDebtRequest req) {
        return service.payDebt(req);
    }

    /**
     * Đánh dấu các nợ quá hạn (nên chạy scheduled job)
     */
    @PostMapping("/mark-overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public int markOverdue() {
        return service.markOverdueDebts();
    }
}
