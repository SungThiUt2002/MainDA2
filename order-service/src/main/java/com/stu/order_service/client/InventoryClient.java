package com.stu.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "inventory-service", url = "${app.inventory-service.url}")
public interface InventoryClient {
    @GetMapping("/api/v1/inventory-items/products/{productId}/available-quantity")
    Integer getAvailableQuantity(@PathVariable Long productId);
}
