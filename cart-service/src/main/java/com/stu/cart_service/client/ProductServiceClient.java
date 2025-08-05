package com.stu.cart_service.client;


import com.stu.cart_service.client.dto.ProductInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${app.product-service.url}")
public interface ProductServiceClient {

    @GetMapping("/api/v1/products/client/{productId}")
    ProductInfo getProductForClient(@PathVariable Long productId);

}