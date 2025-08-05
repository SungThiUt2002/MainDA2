package com.stu.product_service.repository;

import com.stu.product_service.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    // Lấy tất cả ảnh chung của sản phẩm
    List<ProductImage> findByProductId(Long productId);
    
    // Lấy ảnh thumbnail của sản phẩm
    Optional<ProductImage> findByProductIdAndIsThumbnailTrue(Long productId);

    // Kiểm tra sản phẩm có ảnh không
    boolean existsByProductId(Long productId);
    
    // Đếm số ảnh của sản phẩm
    long countByProductId(Long productId);

    
    // Kiểm tra URL duy nhất trong cùng 1 sản phẩm
    boolean existsByProductIdAndUrl(Long productId, String url);
    
    // Kiểm tra URL duy nhất toàn hệ thống
    boolean existsByUrl(String url);
    
    // Tìm ảnh theo URL
    Optional<ProductImage> findByUrl(String url);
} 