package com.stu.common_dto.event.ShippingEvent;

import com.stu.common_dto.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShippingRequestEvent {
    private String orderId;
    private Long userId;
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
   private PaymentMethod paymentMethod;
    private String note;
    private LocalDateTime paidAt;
}
