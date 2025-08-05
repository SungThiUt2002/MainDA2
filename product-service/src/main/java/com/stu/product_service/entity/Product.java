package com.stu.product_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(
    name = "products",
    indexes = {
        @Index(name = "idx_product_category", columnList = "category_id"),
        @Index(name = "idx_product_brand", columnList = "brand_id"),
        @Index(name = "idx_product_is_active", columnList = "is_active")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uc_product_name", columnNames = {"name"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price; // Giá sản phẩm

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

//
//    private Integer stock; // số lượng tồm kho
//    private Integer soldQuantity = 0; // số lượng sản phẩm đã bán


    @Column(name = "is_active")
    private Boolean isActive = true; // ví dụ như hết hàng thì = false(tạm thời không bán)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
//
//    private LocalDateTime deletedAt;
//    private Long createdBy;
//    private Long updatedBy;
//    private Long deletedBy;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProductImage> images;
//
//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ProductVariant> variants;
//
}