package com.stu.account_service.client;

import com.stu.account_service.client.dto.CartRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
;

@FeignClient(name = "cart-service", url = "${app.cart-service.url}")
public interface CartServiceClient {
    @PostMapping("/api/carts/internal")
    void createCartForUser(@RequestBody CartRequest request);
} 