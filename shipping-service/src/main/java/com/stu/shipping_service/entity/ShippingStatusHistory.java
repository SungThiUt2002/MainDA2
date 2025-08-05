package com.stu.shipping_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.stu.common_dto.enums.ShippingStatus;
import lombok.*;

@Entity
@Table(name = "shipping_status_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_order_id", nullable = false)
    private ShippingOrder shippingOrder;//Đơn vận chuyển liên kết

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShippingStatus status;//rạng thái vận chuyển tại thời điểm này

    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt; //Thời điểm thay đổi trạng thái

    private String description;//: Mô tả/ghi chú khi thay đổi trạng thái
} 