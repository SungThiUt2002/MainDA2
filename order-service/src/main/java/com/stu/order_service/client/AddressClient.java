package com.stu.order_service.client;

import com.stu.order_service.client.dto.OrderShippingAddressResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "account-service", url = "${app.account-service.url}")
public interface AddressClient {
    @GetMapping("/api/address/{id}/users/{userId}")
    OrderShippingAddressResponse getOnAddress(@PathVariable Long id,@PathVariable Long userId);
}
