package com.stu.payment_service.dto.request;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateRefundRequest {
    /** Mã thanh toán cần hoàn */
    private Long paymentId;
    /** Số tiền hoàn */
    private BigDecimal amount;
    /** Mô tả lý do hoàn tiền (nếu có) */
    private String description;
} 