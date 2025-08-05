package com.stu.shipping_service.repository;

import com.stu.shipping_service.entity.ShippingOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShippingOrderRepository extends JpaRepository<ShippingOrder, Long> {
    Optional<ShippingOrder> findByOrderId (String orderId);
     boolean existsByOrderId(String orderId);
} 