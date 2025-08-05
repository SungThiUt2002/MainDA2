package com.stu.shipping_service.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.stu.common_dto.enums.ShippingStatus;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "shipping_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingOrder {
    /** Mã định danh đơn vận chuyển */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã đơn hàng liên kết (từ order-service) */
    @Column(nullable = false)
    private String orderId;

    /** Địa chỉ giao hàng */
    @Column(nullable = false)
    private String shippingAddress;//snapshot tại thời điểm nhận event từ oorder service


    @Column(nullable = false)
    private String receiverName;//Tên người nhận (snapshot) từ order service

    /** Số điện thoại người nhận */
    @Column(nullable = false)
    private String receiverPhone;

    /** Đơn vị vận chuyển (GHN, GHTK, ...) */
    @Column(nullable = false)
    private String shippingMethod;

    /** Mã vận đơn của đối tác vận chuyển */
    private String trackingCode;

    /** Trạng thái hiện tại của đơn vận chuyển */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShippingStatus status;
//
//    /** Phí vận chuyển */
    private BigDecimal shippingFee;

    /** Ghi chú thêm cho đơn vận chuyển */
    private String note;

    /** Danh sách lịch sử thay đổi trạng thái của đơn vận chuyển */
    @OneToMany(mappedBy = "shippingOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShippingStatusHistory> statusHistory;

    /** Thời điểm tạo đơn vận chuyển */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Thời điểm cập nhật cuối cùng */
    private LocalDateTime updatedAt;
} 