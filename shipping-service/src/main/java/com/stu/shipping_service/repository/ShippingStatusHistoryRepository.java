package com.stu.shipping_service.repository;

import com.stu.shipping_service.entity.ShippingStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingStatusHistoryRepository extends JpaRepository<ShippingStatusHistory, Long> {
} 