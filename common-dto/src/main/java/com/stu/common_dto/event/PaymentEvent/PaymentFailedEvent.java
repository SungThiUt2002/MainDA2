package com.stu.common_dto.event.PaymentEvent;

import com.stu.common_dto.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFailedEvent {
    private String orderId;               // ID đơn hàng
//    private Long paymentId;              // ID thanh toán
    private Long userId;
    private String reason;
//    private BigDecimal amount;           // Số tiền đã thanh toán
//    private PaymentMethod paymentMethod; // Phương thức thanh toán
private LocalDateTime failedAt;        // Thời điểm xác nhận thanh toán thất bại
}