package com.stu.shipping_service.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateShippingOrderRequest {
    private String orderId;
    private String shippingAddress;
    private String receiverName;
    private String receiverPhone;
    private String shippingMethod;
//    private String trackingCode;
//    private BigDecimal shippingFee;
    private String note;
} 