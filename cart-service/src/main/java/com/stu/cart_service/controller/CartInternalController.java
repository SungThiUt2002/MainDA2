package com.stu.cart_service.controller;

import com.stu.cart_service.dto.request.CartRequest;
import com.stu.cart_service.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts/internal")
@RequiredArgsConstructor
public class CartInternalController {
    private final CartService cartService;

    // 1. Tạo giỏ hàng (được gọi khi đăng ký tài khoản, USER không dc tự tạo)
    @PostMapping
    public ResponseEntity<Void> createCartForUser(@RequestBody CartRequest request) {
        cartService.createCart(request);
        return ResponseEntity.ok().build();
    }
} 