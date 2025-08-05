package com.stu.account_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_address")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderShippingAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    // Tài khoản sở hữu địa chỉ
//    private Long userId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User users;

    private String receiverName; // tên người nhận
    private String receiverPhone; // số điện thoại người nhận
    private String province; // tỉnh
    private String district; // Huyện
    private String ward; // xã, phường
    private String streetAddress; // đìa chỉ cụ thể, thôn, xóm, số nhà, đường .

}
