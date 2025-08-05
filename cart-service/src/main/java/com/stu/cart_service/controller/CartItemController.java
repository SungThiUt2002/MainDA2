package com.stu.cart_service.controller;

import com.stu.cart_service.dto.request.CartItemRequest;
import com.stu.cart_service.dto.response.CartItemResponse;
import com.stu.cart_service.service.CartItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/cart-items")
@RequiredArgsConstructor
public class CartItemController {
    private final CartItemService cartItemService;

    // Lấy chi tiết cart item
    @GetMapping("/{cartItemId}")
    public ResponseEntity<CartItemResponse> getCartItem(@PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartItemService.getCartItemById(cartItemId));
    }

//    // Lấy tất cả cart item theo cartId
//    @GetMapping("/cart/{cartId}")
//    public ResponseEntity<List<CartItemResponse>> getCartItemsByCart(@PathVariable Long cartId) {
//        return ResponseEntity.ok(cartItemService.getCartItemsByCart(cartId));
//    }

//    // Tạo mới cart item (ít dùng, thường thao tác qua CartController)
//    @PostMapping("/cart/{cartId}")
//    public ResponseEntity<CartItemResponse> createCartItem(
//            @PathVariable Long cartId,
//            @Valid @RequestBody CartItemRequest request) {
//        return ResponseEntity.ok(cartItemService.createCartItem(cartId, request));
//    }

//    // Xóa cart item
//    @DeleteMapping("/{cartItemId}")
//    public ResponseEntity<Void> deleteCartItem(@PathVariable Long cartItemId) {
//        cartItemService.deleteCartItem(cartItemId);
//        return ResponseEntity.noContent().build();
//    }

} 