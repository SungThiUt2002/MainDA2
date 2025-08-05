package com.stu.product_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // đảm bảo tên mỗi danh mục là duy nhất
    @Column(nullable = false, length = 255, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
//
//    @Column(length = 500)
//    private String imageUrl;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;
}

/*
Lưu thông tin danh mục sản phẩm (ví dụ: Đồ gia dụng, Thiết bị nhà bếp...).
Lưu trữ:
Tên, mô tả danh mục.
Danh sách sản phẩm thuộc danh mục này.
 */