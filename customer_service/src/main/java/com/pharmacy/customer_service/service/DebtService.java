package com.pharmacy.customer_service.service;

import com.pharmacy.customer_service.dto.CreateDebtRequest;
import com.pharmacy.customer_service.dto.PayDebtRequest;
import com.pharmacy.customer_service.entity.Customer;
import com.pharmacy.customer_service.entity.CustomerDebt;
import com.pharmacy.customer_service.repository.CustomerDebtRepo;
import com.pharmacy.customer_service.repository.CustomerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DebtService {

    private final CustomerDebtRepo debtRepo;
    private final CustomerRepo customerRepo;

    public List<CustomerDebt> getCustomerDebts(Long customerId) {
        return debtRepo.findByCustomerId(customerId);
    }

    public List<CustomerDebt> getCustomerPendingDebts(Long customerId) {
        return debtRepo.findByCustomerIdAndStatus(customerId, "PENDING");
    }

    public BigDecimal getTotalRemainingDebt(Long customerId) {
        BigDecimal total = debtRepo.sumRemainingDebt(customerId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional
    public CustomerDebt createDebt(CreateDebtRequest req) {
        Customer customer = customerRepo.findById(req.customerId())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + req.customerId()));

        // Kiểm tra credit limit
        BigDecimal currentDebt = getTotalRemainingDebt(req.customerId());
        BigDecimal newTotal = currentDebt.add(req.amount());

        if (customer.getCreditLimit().compareTo(BigDecimal.ZERO) > 0 
            && newTotal.compareTo(customer.getCreditLimit()) > 0) {
            throw new RuntimeException("Exceeds credit limit. Current debt: " + currentDebt 
                + ", Credit limit: " + customer.getCreditLimit());
        }

        CustomerDebt debt = new CustomerDebt();
        debt.setCustomerId(req.customerId());
        debt.setInvoiceId(req.invoiceId());
        debt.setInvoiceCode(req.invoiceCode());
        debt.setAmount(req.amount());
        debt.setDueDate(req.dueDate());
        debt.setNotes(req.notes());

        CustomerDebt saved = debtRepo.save(debt);

        // Cập nhật current debt của customer
        customer.setCurrentDebt(newTotal);
        customerRepo.save(customer);

        return saved;
    }

    @Transactional
    public CustomerDebt payDebt(PayDebtRequest req) {
        CustomerDebt debt = debtRepo.findById(req.debtId())
                .orElseThrow(() -> new RuntimeException("Debt not found: " + req.debtId()));

        if ("PAID".equals(debt.getStatus())) {
            throw new RuntimeException("Debt already paid");
        }

        BigDecimal remaining = debt.getRemainingAmount();
        if (req.amount().compareTo(remaining) > 0) {
            throw new RuntimeException("Payment amount exceeds remaining debt: " + remaining);
        }

        BigDecimal newPaid = debt.getPaidAmount().add(req.amount());
        debt.setPaidAmount(newPaid);

        if (newPaid.compareTo(debt.getAmount()) >= 0) {
            debt.setStatus("PAID");
        } else {
            debt.setStatus("PARTIAL");
        }

        if (req.notes() != null) {
            debt.setNotes(debt.getNotes() == null ? req.notes() : debt.getNotes() + "\n" + req.notes());
        }

        CustomerDebt saved = debtRepo.save(debt);

        // Cập nhật current debt của customer
        Customer customer = customerRepo.findById(debt.getCustomerId()).orElseThrow();
        BigDecimal newDebt = getTotalRemainingDebt(debt.getCustomerId());
        customer.setCurrentDebt(newDebt);
        customerRepo.save(customer);

        return saved;
    }

    /**
     * Đánh dấu các nợ quá hạn
     */
    @Transactional
    public int markOverdueDebts() {
        List<CustomerDebt> overdueDebts = debtRepo.findOverdueDebts();
        for (CustomerDebt debt : overdueDebts) {
            debt.setStatus("OVERDUE");
            debtRepo.save(debt);
        }
        return overdueDebts.size();
    }
}
