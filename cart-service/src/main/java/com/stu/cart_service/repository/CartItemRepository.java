package com.stu.cart_service.repository;

import com.stu.cart_service.entity.CartItem;
import com.stu.cart_service.entity.Cart;
import com.stu.cart_service.enums.CartItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart(Cart cart);
    Optional<CartItem> findByCartAndProductId(Cart cart, Long productVariantId);
    List<CartItem> findByCartAndStatus(Cart cart, CartItemStatus status);
    List<CartItem> findByProductId(Long productId);
    Optional<CartItem> findItemByProductId( Long productId);
//    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

} 