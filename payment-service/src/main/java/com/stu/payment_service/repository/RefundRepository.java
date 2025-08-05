package com.stu.payment_service.repository;

import com.stu.payment_service.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    // Có thể bổ sung các hàm query method khác nếu cần
} 