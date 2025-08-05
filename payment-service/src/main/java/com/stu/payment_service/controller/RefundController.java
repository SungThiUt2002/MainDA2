package com.stu.payment_service.controller;

import com.stu.payment_service.dto.request.CreateRefundRequest;
import com.stu.payment_service.dto.response.RefundResponse;
import com.stu.payment_service.enums.PaymentStatus;
import com.stu.payment_service.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {
    private final RefundService refundService;

    /**
     * Tạo mới một yêu cầu hoàn tiền
     */
    @PostMapping
    public ResponseEntity<RefundResponse> createRefund(@RequestBody CreateRefundRequest request) {
        return ResponseEntity.ok(refundService.createRefund(request));
    }

    /**
     * Xác nhận hoàn tiền (thành công/thất bại)
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<RefundResponse> confirmRefund(
            @PathVariable Long id,
            @RequestParam PaymentStatus status) {
        return ResponseEntity.ok(refundService.confirmRefund(id, status));
    }

    /**
     * Lấy thông tin hoàn tiền theo id
     */
    @GetMapping("/{id}")
    public ResponseEntity<RefundResponse> getRefundById(@PathVariable Long id) {
        return ResponseEntity.ok(refundService.getRefundById(id));
    }

    /**
     * Lấy danh sách tất cả hoàn tiền
     */
    @GetMapping
    public ResponseEntity<List<RefundResponse>> listRefunds() {
        return ResponseEntity.ok(refundService.listRefunds());
    }
} 