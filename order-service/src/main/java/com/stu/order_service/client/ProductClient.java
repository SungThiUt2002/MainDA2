package com.stu.order_service.client;

import com.stu.order_service.client.dto.ProductInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${app.product-service.url}")
public interface ProductClient {
//    @GetMapping("/api/products/client/products/{productId}/variants/{variantId}")
//    ProductInfo getProductVariant(
//            @PathVariable Long productId,
//            @PathVariable Long variantId
//    );

    @GetMapping("/api/v1/products/{productId}")
    ProductInfo getProduct(@PathVariable Long productId);
}
