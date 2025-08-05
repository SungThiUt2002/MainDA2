package com.stu.payment_service.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.stu.payment_service.enums.PaymentStatus;
import com.stu.common_dto.enums.PaymentMethod;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class
Payment {
    // Mã định danh thanh toán
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mã đơn hàng liên kết (từ order-service)
    @Column(nullable = false)
    private String orderId;

    /** Mã người dùng thực hiện thanh toán (từ account-service) */
    @Column(nullable = false)
    private Long userId;

    /** Số tiền thanh toán */
    @Column(nullable = false)
    private BigDecimal amount;

    /** Loại tiền tệ (VND, USD, ...) */
    @Column(nullable = false)
    private String currency;

    /** Phương thức thanh toán */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    /** Trạng thái thanh toán */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    /** Mã giao dịch với cổng thanh toán ngoài (nếu có) */
    private String transactionId;

    /** Mô tả thêm về giao dịch */
    private String description;

    /** Thời điểm tạo thanh toán */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Thời điểm cập nhật cuối */
    private LocalDateTime updatedAt;

    /** Danh sách các giao dịch liên quan đến thanh toán này */
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentTransaction> transactions;

    /** Danh sách các yêu cầu hoàn tiền liên quan đến thanh toán này */
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Refund> refunds;
} 