package com.stu.cart_service.entity;

import com.stu.cart_service.enums.CartItemStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho bảng cart_items (sản phẩm trong giỏ hàng)
 */
@Entity
@Table(name = "cart_items",
    uniqueConstraints = @UniqueConstraint(name = "uq_cart_product", columnNames = {"cart_id", "product_variant_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nhiều cart item thuộc về một cart
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private Long productId; // ID sản phẩm (product-service)

    private String productName; // lấy từ product service

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", precision = 12, scale = 2, nullable = false)
    private BigDecimal price; // Giá tại thời điểm thêm vào cart( nhận event từ product service để cập nhập)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private CartItemStatus status; // ACTIVE, REMOVED...

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 