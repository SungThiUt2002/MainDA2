package com.stu.shipping_service.dto.response;

import com.stu.common_dto.enums.ShippingStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShippingOrderResponse {
    private Long id;
    private String orderId;
    private String shippingAddress;
    private String receiverName;
    private String receiverPhone;
    private String shippingMethod;
    private String trackingCode;
    private ShippingStatus status;
    private BigDecimal shippingFee;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 