package com.stu.order_service.dto.request;


import com.stu.common_dto.enums.PaymentMethod;
import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    private Long userId;
//    private String note;
//    private PaymentMethod paymentMethod;
//    private Long addressId;
    private List<OrderItemRequest> items;
} 