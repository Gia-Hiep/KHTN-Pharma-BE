package com.pharmacy.sales_service.job;

import com.pharmacy.sales_service.client.InventoryClient;
import com.pharmacy.sales_service.entity.Invoice;
import com.pharmacy.sales_service.repository.InvoiceRepo;
import com.pharmacy.sales_service.repository.InvoiceLotAllocationRepo;
import com.pharmacy.sales_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tự động release reservation cho invoice WAIT_PAYMENT quá lâu không thanh toán.
 * Giải phóng tồn kho bị lock, đưa invoice về DRAFT để dược sĩ có thể checkout lại.
 *
 * Cấu hình trong application.properties:
 *   reservation.timeout-minutes=15   (default: 15 phút)
 *   reservation.cleanup-interval-ms=120000  (default: 2 phút)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCleanupJob {

    private final InvoiceRepo invoiceRepo;
    private final InvoiceLotAllocationRepo allocRepo;
    private final InventoryClient inventoryClient;
    private final JwtService jwtService;

    @Value("${reservation.timeout-minutes:15}")
    private int timeoutMinutes;

    /**
     * Chạy mỗi 2 phút, tìm invoice WAIT_PAYMENT quá hạn → release reservation → đưa về DRAFT.
     */
    @Scheduled(fixedDelayString = "${reservation.cleanup-interval-ms:120000}")
    @Transactional
    public void cleanupStaleReservations() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(timeoutMinutes);
        List<Invoice> stale = invoiceRepo.findByStatusAndUpdatedAtBefore("WAIT_PAYMENT", cutoff);

        if (stale.isEmpty()) return;

        log.info("⏰ Found {} stale WAIT_PAYMENT invoices (timeout={}min). Releasing...", stale.size(), timeoutMinutes);

        // Tạo system token cho internal call tới inventory-service
        String systemToken = jwtService.generateSystemToken();

        for (Invoice inv : stale) {
            try {
                // Release reservation trong inventory-service
                inventoryClient.release(systemToken, "INVOICE", inv.getCode());

                // Xóa allocations
                allocRepo.deleteByInvoiceId(inv.getId());

                // Đưa invoice về DRAFT → dược sĩ có thể checkout lại
                inv.setStatus("DRAFT");
                inv.setUpdatedAt(LocalDateTime.now());
                inv.setNotes((inv.getNotes() == null ? "" : inv.getNotes() + " | ") +
                        "⏰ Tự động giải phóng tồn kho sau " + timeoutMinutes + " phút không thanh toán");
                invoiceRepo.save(inv);

                log.info("  ✅ Released invoice #{} (code={})", inv.getId(), inv.getCode());
            } catch (Exception e) {
                log.error("  ❌ Failed to release invoice #{}: {}", inv.getId(), e.getMessage());
            }
        }
    }
}
