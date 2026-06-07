package com.pharmacy.sales_service.repository;

import com.pharmacy.sales_service.entity.InvoiceLotAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InvoiceLotAllocationRepo extends JpaRepository<InvoiceLotAllocation, Long> {
    List<InvoiceLotAllocation> findByInvoiceItemId(Long invoiceItemId);

    @Modifying
    @Query("""
            delete from InvoiceLotAllocation a
            where a.invoiceItemId in (
              select it.id from InvoiceItem it where it.invoiceId = :invoiceId
            )
            """)
    void deleteByInvoiceId(Long invoiceId);

}
