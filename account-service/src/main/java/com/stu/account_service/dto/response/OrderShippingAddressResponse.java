package com.stu.account_service.dto.response;

import lombok.Data;

@Data
public class OrderShippingAddressResponse {
    private Long id;
    private String receiverName;
    private String receiverPhone;
    private String province; // tỉnh
    private String district; // Huyện
    private String ward; // xã, phường
    private String streetAddress; // đìa chỉ cụ thể, thôn, xóm, số nhà, đường ...
}