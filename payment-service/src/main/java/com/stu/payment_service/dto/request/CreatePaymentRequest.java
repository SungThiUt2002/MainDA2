package com.stu.payment_service.dto.request;

import com.stu.common_dto.enums.PaymentMethod;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreatePaymentRequest {
    private String orderId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private String description;
} 