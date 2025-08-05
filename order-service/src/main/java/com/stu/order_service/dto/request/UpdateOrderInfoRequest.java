package com.stu.order_service.dto.request;

import com.stu.common_dto.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderInfoRequest {
    @NotNull(message = "Địa chỉ không được để trống")
    private Long addressId;
    private String note;
    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

}
