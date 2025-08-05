package com.stu.order_service.controller;

import com.stu.order_service.dto.request.UpdateSatusShippingRequest;
import com.stu.order_service.dto.response.OrderResponse;
import com.stu.order_service.entity.Order;
import com.stu.order_service.enums.OrderStatus;
import com.stu.order_service.mapper.OrderMapper;
import com.stu.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/apis/v1/admin/orders")
@RequiredArgsConstructor
public class OrderControllerAdmin {
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    // 1. Lấy tất cả đơn hàng chờ xác nhận (PENDING_CONFIRMATION)
    @GetMapping("/pending-confirmation")
    public ResponseEntity<List<OrderResponse>> getPendingConfirmationOrders() {
        var response = orderService.getPendingConfirmationOrders();
        return ResponseEntity.ok(response);
    }

    // 2. Lấy chi tiết đơn hàng theo ID
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String orderId) {
        var response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }

    // 3. Lấy đơn hàng theo trạng thái
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
        var response = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(response);
    }

    // 4. Lấy tất cả đơn hàng
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        var response = orderService.getAllOrders();
        return ResponseEntity.ok(response);
    }

    // 4b. Lấy tất cả đơn hàng (endpoint cụ thể)
    @GetMapping("/all")
    public ResponseEntity<List<OrderResponse>> getAllOrdersSpecific() {
        var response = orderService.getAllOrders();
        return ResponseEntity.ok(response);
    }

    // 5. Admin xác nhận đơn hàng COD
    @PutMapping("/{orderId}/confirm")
    public ResponseEntity<Void> adminConfirmOrder(@PathVariable String orderId) {
        orderService.confirmOrderByAdmin(orderId);
        return ResponseEntity.ok().build();
    }

    // 6. Lấy tổng số đơn hàng - cho Admin Dashboard
    @GetMapping("/count")
    public ResponseEntity<Long> getTotalOrderCount() {
        Long totalCount = orderService.getTotalOrderCount();
        return ResponseEntity.ok(totalCount);
    }

    // 7. Cập nhật trạng thái vận chuyển (Admin)
    @PutMapping("/{orderId}/shipping-status")
    public ResponseEntity<OrderResponse> updateShippingStatus(
            @PathVariable String orderId,
            @RequestBody UpdateSatusShippingRequest request) {
        Order updatedOrder = orderService.updateShippingStatus(orderId, request);
        OrderResponse response = orderMapper.toOrderResponse(updatedOrder);
        return ResponseEntity.ok(response);
    }
} 