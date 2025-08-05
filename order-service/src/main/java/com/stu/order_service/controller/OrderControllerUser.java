package com.stu.order_service.controller;

import com.stu.common_dto.enums.PaymentMethod;
import com.stu.order_service.dto.request.UpdateOrderInfoRequest;
import com.stu.order_service.dto.response.OrderResponse;
import com.stu.order_service.entity.Order;
import com.stu.order_service.enums.OrderStatus;
import com.stu.order_service.mapper.OrderMapper;
import com.stu.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/apis/v1/users/orders")
@RequiredArgsConstructor
public class OrderControllerUser {
    private final OrderService orderService;
    private final OrderMapper orderMapper;



    // 1. Hập địa chỉ giao hàng, phương thức thanh toán cho đơn hàng (đã được tạo từ checkout listener)
    @PutMapping("/{id}/info")
    public ResponseEntity<OrderResponse> updateOrderInfo(@PathVariable("id") String orderId, @RequestBody @Valid UpdateOrderInfoRequest request) {
        var updatedOrder = orderService.updateOrderInfo(orderId, request);
        orderMapper.updateOrderFromDto(request,updatedOrder);
        var response = orderMapper.toOrderResponse(updatedOrder);

        return ResponseEntity.ok(response);
    }

    // 2. Tạo đơn hàng với đầy đủ thông tin
    @PutMapping("/{id}/confirm")
    public ResponseEntity<Void> confirmOrder(@PathVariable("id") String orderId) {
        orderService.confirmCreateOrder(orderId);
        return ResponseEntity.ok().build();
    }

    // 3. Hủy đơn hàng
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable String orderId,
            @RequestHeader("Authorization") String authorizationHeader){
        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
        orderService.cancelOrder(orderId, token);
        return ResponseEntity.ok().build();
    }

    // 4. Lấy đơn hàng mới nhất của user (sau khi checkout cart)
    @GetMapping("/latest")
    public ResponseEntity<OrderResponse> getLatestOrder(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
        var response = orderService.getLatestOrder(token);
        return ResponseEntity.ok(response);
    }

    // 5. Lấy tất cả đơn hàng của user (cho lịch sử đơn hàng)
    @GetMapping("/history")
    public ResponseEntity<List<OrderResponse>> getUserOrders(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
        var response = orderService.getUserOrders(token);
        return ResponseEntity.ok(response);
    }

    // 6. Lấy đơn hàng của user theo trạng thái
    @GetMapping("/history/status/{status}")
    public ResponseEntity<List<OrderResponse>> getUserOrdersByStatus(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable("status") String status) {
        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
        
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            var response = orderService.getUserOrdersByStatus(token, orderStatus);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 5. Lấy danh sách các phương thức thanh toán
    @GetMapping("/payment-methods")
    public ResponseEntity<List<PaymentMethodResponse>> getPaymentMethods() {
        List<PaymentMethodResponse> paymentMethods = Arrays.stream(PaymentMethod.values())
                .map(method -> new PaymentMethodResponse(method.name(), getPaymentMethodDisplayName(method)))
                .collect(Collectors.toList());
        return ResponseEntity.ok(paymentMethods);
    }

    private String getPaymentMethodDisplayName(PaymentMethod method) {
        switch (method) {
            case COD:
                return "Thanh toán khi nhận hàng (COD)";
            case BANK_TRANSFER:
                return "Chuyển khoản ngân hàng";
            case VNPAY:
                return "Thanh toán qua VNPAY";
            case MOMO:
                return "Thanh toán qua MOMO";
            case PAYPAL:
                return "Thanh toán qua PayPal";
            default:
                return method.name();
        }
    }

    // DTO cho response
    public static class PaymentMethodResponse {
        private String value;
        private String displayName;

        public PaymentMethodResponse(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }

}
