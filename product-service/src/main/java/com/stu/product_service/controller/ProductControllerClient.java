package com.stu.product_service.controller;

import com.stu.product_service.controller.dtoClient.ProductInfo;
import com.stu.product_service.mapper.ProductMapper;
import com.stu.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products/client")
@RequiredArgsConstructor
public class ProductControllerClient {
    private  final ProductService productService;
    private  final ProductMapper productMapper;

    // chỉ dùng cho các service khác gọi để lấy thông tin sản phẩm
    @GetMapping("/{productId}")
    public ResponseEntity<ProductInfo> getProductForClient(@PathVariable Long productId) {
        var product = productService.getProductById(productId);
        var response = productMapper.toResponseClient(product);
        return ResponseEntity.ok(response);
    }
}
