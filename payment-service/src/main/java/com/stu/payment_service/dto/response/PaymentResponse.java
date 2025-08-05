package com.stu.payment_service.dto.response;

import com.stu.payment_service.enums.PaymentStatus;
import com.stu.common_dto.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class PaymentResponse {
    private Long id;
    private String orderId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionId;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 