package com.stu.profile_service.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderShippingAddressRequest {
    private Long userId;
    private String receiverName;
    private String receiverPhone;
    private String province; // tỉnh
    private String district; // Huyện
    private String ward; // xã, phường
    private String streetAddress; // đìa chỉ cụ thể, thôn, xóm, số nhà, đường .
}