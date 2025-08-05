package com.stu.payment_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.stu.payment_service.enums.TransactionAction;
import com.stu.payment_service.enums.PaymentStatus;
import lombok.*;

@Entity
@Table(name = "payment_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {
    /** Mã định danh giao dịch */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tham chiếu đến Payment */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    /** Hành động giao dịch (INIT, PAY, REFUND, CANCEL) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionAction action;

    /** Trạng thái giao dịch */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    /** Phản hồi từ cổng thanh toán (nếu có) */
    @Lob
    private String gatewayResponse;

    /** Thời điểm thực hiện giao dịch */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
} 