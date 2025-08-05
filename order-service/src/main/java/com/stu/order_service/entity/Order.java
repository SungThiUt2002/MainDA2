package com.stu.order_service.entity;

import com.stu.common_dto.enums.PaymentMethod;
import com.stu.order_service.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;

    private Long paymentId;
    private Long shippingId;

    // Optional audit field only
    private Long addressId;

    // Snapshot address info (denormalized)
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String district;
    private String ward;
    private String streetAddress;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String note;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatusHistory> statusHistories;

    @Transient
    public String getFullShippingAddress() {
        return String.join(", ", streetAddress, ward, district, province);
    }
}

//@Transient
//Đây là annotation của Jakarta Persistence (JPA).
//Có nghĩa: phương thức này không ánh xạ (persist) vào database.
//Nó chỉ được sử dụng tạm thời trong Java runtime, không liên quan tới bảng trong DB.
