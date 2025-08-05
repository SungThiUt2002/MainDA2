package com.stu.shipping_service.controller;

import com.stu.shipping_service.dto.request.CreateShippingOrderRequest;
import com.stu.shipping_service.dto.request.UpdateSatusShippingRequest;
import com.stu.shipping_service.dto.response.ShippingOrderResponse;
import com.stu.shipping_service.dto.response.ShippingStatusHistoryResponse;
import com.stu.common_dto.enums.ShippingStatus;
import com.stu.shipping_service.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {
    private final ShippingService shippingService;

//    /**
//     * Tạo mới một đơn vận chuyển
//     */
//    @PostMapping
//    public ResponseEntity<ShippingOrderResponse> createShippingOrder(@RequestBody CreateShippingOrderRequest request) {
//        return ResponseEntity.ok(shippingService.createShippingOrder(request));
//    }

    /**
     * Cập nhật trạng thái đơn vận chuyển (ADMIN)
     */
    @PostMapping("/{orderId}/status")
    public ResponseEntity<ShippingOrderResponse> updateShippingStatus(
            @PathVariable String orderId, @RequestBody UpdateSatusShippingRequest request)  {
        return ResponseEntity.ok(shippingService.updateShippingStatus(orderId, request));
    }

    /**
     * Lấy chi tiết một đơn vận chuyển
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShippingOrderResponse> getShippingOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(shippingService.getShippingOrderById(id));
    }

    /**
     * Lấy danh sách tất cả đơn vận chuyển
     */
    @GetMapping
    public ResponseEntity<List<ShippingOrderResponse>> listShippingOrders() {
        return ResponseEntity.ok(shippingService.listShippingOrders());
    }

    /**
     * Lấy lịch sử trạng thái của một đơn vận chuyển
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<List<ShippingStatusHistoryResponse>> getShippingStatusHistory(@PathVariable Long id) {
        return ResponseEntity.ok(shippingService.getShippingStatusHistory(id));
    }
} 