package com.pharmacy.purchase_service.repository;

import com.pharmacy.purchase_service.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepo extends JpaRepository<Supplier, Long> {}
