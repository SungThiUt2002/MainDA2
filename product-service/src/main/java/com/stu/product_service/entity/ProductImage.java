package com.stu.product_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ảnh chung cho sản phẩm (bắt buộc)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

//    // Ảnh riêng cho biến thể (nullable)
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "product_variant_id")
//    private ProductVariant productVariant;

    @Column(nullable = false, length = 500)
    private String url;

    private Boolean isThumbnail; // = true --> ảnh đại diện
}
/*
ProductImage - Hỗ trợ cả ảnh chung và ảnh riêng cho biến thể
Chức năng:
- Lưu các ảnh của sản phẩm (ảnh chung cho toàn bộ sản phẩm)
- Lưu các ảnh riêng cho từng biến thể sản phẩm (màu sắc, kích thước...)
Lưu trữ:
- Đường dẫn ảnh (URL)
- Đánh dấu ảnh đại diện (isThumbnail)
- Mô tả ảnh (altText)
- Liên kết với sản phẩm (Product) - bắt buộc
- Liên kết với biến thể (ProductVariant) - tùy chọn
Cách sử dụng:
- Ảnh chung: chỉ có product, productVariant = null
- Ảnh riêng cho variant: có cả product và productVariant
 */