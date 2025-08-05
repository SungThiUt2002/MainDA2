package com.stu.payment_service.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.stu.payment_service.enums.PaymentStatus;
import lombok.*;
import lombok.ToString;

@Entity
@Table(name = "refunds")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refund {
    /** Mã định danh hoàn tiền */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tham chiếu đến Payment */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    /** Số tiền hoàn */
    @Column(nullable = false)
    private BigDecimal amount;

    /** Trạng thái hoàn tiền */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    /** Thời điểm tạo yêu cầu hoàn tiền */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Thời điểm cập nhật cuối */
    private LocalDateTime updatedAt;
} 