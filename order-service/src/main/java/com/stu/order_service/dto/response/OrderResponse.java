package com.stu.order_service.dto.response;


import com.stu.common_dto.enums.PaymentMethod;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private String id;
    private Long userId;
    private String status;
    private BigDecimal totalAmount;

    private String receiverName;
    private String receiverPhone;
    private String province; // tỉnh
    private String district; // Huyện
    private String ward; // xã, phường
    private String streetAddress; // đìa chỉ cụ thể, thôn, xóm, số nhà, đường ...

    private String note;
    private PaymentMethod paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;
} 