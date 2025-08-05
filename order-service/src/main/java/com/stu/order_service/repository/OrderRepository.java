package com.stu.order_service.repository;

import com.stu.order_service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    
    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC LIMIT 1")
    Optional<Order> findTopByOrderByCreatedAtDesc();
    
    // Tìm đơn hàng mới nhất của một user cụ thể
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC LIMIT 1")
    Optional<Order> findTopByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    // Lấy tất cả đơn hàng của user theo thứ tự mới nhất
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    // Lấy đơn hàng của user theo trạng thái
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status = :status ORDER BY o.createdAt DESC")
    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("status") com.stu.order_service.enums.OrderStatus status);
    
    // ========== ADMIN QUERIES ==========
    
    // Lấy tất cả đơn hàng theo trạng thái
    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt DESC")
    List<Order> findByStatusOrderByCreatedAtDesc(@Param("status") com.stu.order_service.enums.OrderStatus status);
    
    // Lấy tất cả đơn hàng theo thứ tự mới nhất
    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findAllByOrderByCreatedAtDesc();
} 