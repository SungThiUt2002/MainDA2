package com.stu.cart_service.service;

import com.stu.cart_service.dto.request.CartItemRequest;
import com.stu.cart_service.dto.response.CartItemResponse;
import com.stu.cart_service.entity.Cart;
import com.stu.cart_service.entity.CartItem;
import com.stu.cart_service.enums.CartItemStatus;
import com.stu.cart_service.exception.AppException;
import com.stu.cart_service.exception.ErrorCode;
import com.stu.cart_service.mapper.CartItemMapper;
import com.stu.cart_service.repository.CartRepository;
import com.stu.cart_service.repository.CartItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.math.BigDecimal;
import com.stu.cart_service.enums.CartStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartItemService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;


    /**
     * Lấy cart item theo id
     */
    public CartItemResponse getCartItemById(Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
        return cartItemMapper.toResponse(item);
    }

    /**
     * Cập nhật giá, tên sản phẩm trong cart item từ product event(internal - product service)
     * Sử dụng cho Kafka listener
     */
    @Transactional
    public void updateCartItemFromProductEvent(Long productId, BigDecimal newPrice, String productName) {
        log.info("Cập nhật cart items từ product event: productId={}, newPrice={}",
                productId, newPrice);
        
        List<CartItem> cartItems = cartItemRepository.findByProductId(productId);
        
        if (cartItems.isEmpty()) {
            log.info("Không có cart items nào chứa sản phẩm: productId={}", productId);
            return;
        }
        
        int updatedCount = 0;
        for (CartItem item : cartItems) {
            // Kiểm tra business rules
            if (!isCartItemUpdatable(item)) {
                log.warn("Cart item không thể cập nhật: cartItemId={}, status={}", 
                        item.getId(), item.getStatus());
                continue;
            }
            
            boolean hasChanges = false;
            // Cập nhập tên mới
            if (productName != null && !productName.equals(item.getProductName())) {
                item.setProductName(productName);
                hasChanges = true;
            }
            
            // Cập nhật giá mới
            if (newPrice != null && !newPrice.equals(item.getPrice())) {
                item.setPrice(newPrice);
                hasChanges = true;
            }
            
            // Chỉ save nếu có thay đổi
            if (hasChanges) {
                cartItemRepository.save(item);
                updatedCount++;
                log.info("Đã cập nhật cart item: cartItemId={}, newPrice={}",
                        item.getId(), newPrice);
            }
        }
        
        log.info("Hoàn thành cập nhật {} cart items cho sản phẩm: productId={}",
                updatedCount, productId);
    }
    
    /**
     * Đánh dấu cart items bị xóa từ product variant event(internal - product service)
     * Sử dụng cho Kafka listener
     */
    @Transactional
    public void markCartItemsAsRemovedByVariantId(Long productId) {
        log.info("Đánh dấu cart items bị xóa từ product event: variantId={}", productId);

        List<CartItem> cartItems = cartItemRepository.findByProductId(productId);

        if (cartItems.isEmpty()) {
            log.info("Không có cart items nào chứa variant: variantId={}", productId);
            return;
        }

        int removedCount = 0;
        for (CartItem item : cartItems) {
            // Kiểm tra business rules
            if (!isCartItemUpdatable(item)) {
                log.warn("Cart item không thể đánh dấu xóa: cartItemId={}, status={}", item.getId(), item.getStatus());
                continue;
            }
            if (item.getStatus() != CartItemStatus.REMOVED) {
                item.setStatus(CartItemStatus.REMOVED);
                cartItemRepository.save(item);
                removedCount++;
                log.info("Đã đánh dấu cart item bị xóa: cartItemId={}", item.getId());
            }
        }
        log.info("Hoàn thành đánh dấu {} cart items cho variant bị xóa: variantId={}", removedCount, productId);
    }
    
    /**
     * Kiểm tra xem cart item có thể cập nhật không
     */
    private boolean isCartItemUpdatable(CartItem item) {
        // Kiểm tra cart status
        if (item.getCart().getStatus() != CartStatus.ACTIVE) {
            return false;
        }
        
        // Kiểm tra cart item status
        if (item.getStatus() == CartItemStatus.REMOVED) {
            return false;
        }
        
        return true;
    }
} 