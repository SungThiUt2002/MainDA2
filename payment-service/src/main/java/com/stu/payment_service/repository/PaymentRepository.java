package com.stu.payment_service.repository;

import com.stu.payment_service.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Có thể bổ sung các hàm query method khác nếu cần
    Optional<Payment> findByOrderId( String orderId);
    boolean existsByOrderId(String orderId);
} 