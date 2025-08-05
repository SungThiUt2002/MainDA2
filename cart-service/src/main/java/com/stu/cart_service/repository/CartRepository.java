package com.stu.cart_service.repository;

import com.stu.cart_service.entity.Cart;
import com.stu.cart_service.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // Tìm cart theo userId và status
    Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);
    Optional<Cart> findByUserId(Long userId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Cart c WHERE c.userId = :userId")
    void deleteByUserId(Long userId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Cart c SET c.status = :status WHERE c.userId = :userId")
    void updateStatusByUserId(Long userId, CartStatus status);
} 