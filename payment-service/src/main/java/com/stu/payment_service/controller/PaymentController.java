package com.stu.payment_service.controller;

import com.stu.payment_service.dto.request.CreatePaymentRequest;
import com.stu.payment_service.dto.response.PaymentResponse;
import com.stu.payment_service.entity.PaymentTransaction;
import com.stu.payment_service.enums.PaymentStatus;
import com.stu.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    /**
     * Tạo mới một thanh toán
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    /**
     * Xác nhận thanh toán (thành công/thất bại/hủy)
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @PathVariable String id,
            @RequestParam PaymentStatus status,
            @RequestParam(required = false) String gatewayResponse) {

        return ResponseEntity.ok(paymentService.confirmPayment(id, status, gatewayResponse));
    }

    /**
     * Lấy thông tin thanh toán theo id
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    /**
     * Lấy danh sách tất cả thanh toán
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> listPayments() {
        return ResponseEntity.ok(paymentService.listPayments());
    }

    /**
     * Hủy thanh toán
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelPayment(@PathVariable Long id) {
        paymentService.cancelPayment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy lịch sử transaction của một thanh toán
     */
    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<PaymentTransaction>> getPaymentTransactions(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentTransactions(id));
    }
} 