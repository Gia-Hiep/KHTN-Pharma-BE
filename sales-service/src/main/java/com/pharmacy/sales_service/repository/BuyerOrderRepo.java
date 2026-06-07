package com.pharmacy.sales_service.repository;

import com.pharmacy.sales_service.entity.BuyerOrder;
import com.pharmacy.sales_service.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BuyerOrderRepo extends JpaRepository<BuyerOrder, Long> {

    /** BUYER: only own orders */
    List<BuyerOrder> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    /** PHARMACIST: all orders with given status */
    List<BuyerOrder> findByStatusOrderByCreatedAtAsc(OrderStatus status);

    /** PHARMACIST: all orders except DELIVERED/CANCELLED */
    List<BuyerOrder> findByStatusInOrderByCreatedAtAsc(List<OrderStatus> statuses);

    /** Find own order for owner-check */
    Optional<BuyerOrder> findByIdAndBuyerId(Long id, Long buyerId);
}
