package com.pharmacy.sales_service.repository;

import com.pharmacy.sales_service.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface InvoiceItemRepo extends JpaRepository<InvoiceItem, Long> {
    List<InvoiceItem> findByInvoiceId(Long invoiceId);
}
