package com.pharmacy.customer_service.service;

import com.pharmacy.customer_service.dto.CreateLoyaltyTransactionRequest;
import com.pharmacy.customer_service.entity.Customer;
import com.pharmacy.customer_service.entity.LoyaltyTransaction;
import com.pharmacy.customer_service.repository.CustomerRepo;
import com.pharmacy.customer_service.repository.LoyaltyTransactionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final LoyaltyTransactionRepo txRepo;
    private final CustomerRepo customerRepo;

    public List<LoyaltyTransaction> getTransactions(Long customerId) {
        return txRepo.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public int getBalance(Long customerId) {
        Integer sum = txRepo.sumPoints(customerId);
        return sum != null ? sum : 0;
    }

    @Transactional
    public LoyaltyTransaction createTransaction(CreateLoyaltyTransactionRequest req, Long userId) {
        Customer customer = customerRepo.findById(req.customerId())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + req.customerId()));

        // Validate redeem không vượt quá balance
        if ("REDEEM".equals(req.type())) {
            int balance = getBalance(req.customerId());
            if (Math.abs(req.points()) > balance) {
                throw new RuntimeException("Insufficient points. Balance: " + balance);
            }
        }

        LoyaltyTransaction tx = new LoyaltyTransaction();
        tx.setCustomerId(req.customerId());
        tx.setType(req.type());
        tx.setPoints(req.points());
        tx.setReferenceType(req.referenceType());
        tx.setReferenceId(req.referenceId());
        tx.setNote(req.note());
        tx.setCreatedBy(userId);

        LoyaltyTransaction saved = txRepo.save(tx);

        // Update customer loyalty points
        int newBalance = getBalance(req.customerId());
        customer.setLoyaltyPoints(newBalance);
        
        // Auto-upgrade tier based on points
        customer.setTier(calculateTier(newBalance));
        customerRepo.save(customer);

        return saved;
    }

    /**
     * Tích điểm từ invoice
     */
    @Transactional
    public LoyaltyTransaction earnPoints(Long customerId, int points, String invoiceId, Long userId) {
        CreateLoyaltyTransactionRequest req = new CreateLoyaltyTransactionRequest(
                customerId, "EARN", points, "INVOICE", invoiceId, "Points earned from purchase"
        );
        return createTransaction(req, userId);
    }

    /**
     * Đổi điểm
     */
    @Transactional
    public LoyaltyTransaction redeemPoints(Long customerId, int points, String invoiceId, Long userId) {
        CreateLoyaltyTransactionRequest req = new CreateLoyaltyTransactionRequest(
                customerId, "REDEEM", -Math.abs(points), "INVOICE", invoiceId, "Points redeemed"
        );
        return createTransaction(req, userId);
    }

    private String calculateTier(int points) {
        if (points >= 10000) return "VIP";
        if (points >= 5000) return "GOLD";
        if (points >= 1000) return "SILVER";
        return "REGULAR";
    }
}
