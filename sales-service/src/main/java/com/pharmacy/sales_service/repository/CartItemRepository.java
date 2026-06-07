package com.pharmacy.sales_service.repository;

import com.pharmacy.sales_service.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
