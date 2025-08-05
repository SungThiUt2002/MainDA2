package com.stu.payment_service.service;

import com.stu.payment_service.dto.request.CreateRefundRequest;
import com.stu.payment_service.dto.response.RefundResponse;
import com.stu.payment_service.entity.Payment;
import com.stu.payment_service.entity.Refund;
import com.stu.payment_service.enums.PaymentStatus;
import com.stu.payment_service.exception.AppException;
import com.stu.payment_service.exception.ErrorCode;
import com.stu.payment_service.mapper.RefundMapper;
import com.stu.payment_service.repository.PaymentRepository;
import com.stu.payment_service.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundService {
    private final RefundRepository refundRepository;
    private final RefundMapper refundMapper;
    private final PaymentRepository paymentRepository;

    /**
     * Tạo mới một yêu cầu hoàn tiền.
     */
    @Transactional
    public RefundResponse createRefund(CreateRefundRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new AppException(ErrorCode.REFUND_AMOUNT_EXCEEDS);
        }
        if (request.getAmount().compareTo(payment.getAmount()) > 0) {
            throw new AppException(ErrorCode.REFUND_AMOUNT_EXCEEDS);
        }
        Refund refund = refundMapper.toEntity(request);
        refund.setPayment(payment);
        refund.setStatus(PaymentStatus.PENDING);
        refund.setCreatedAt(LocalDateTime.now());
        refund.setUpdatedAt(LocalDateTime.now());
        return refundMapper.toResponse(refundRepository.save(refund));
    }

    /**
     * Xác nhận trạng thái hoàn tiền (thành công/thất bại).
     */
    @Transactional
    public RefundResponse confirmRefund(Long refundId, PaymentStatus status) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new AppException(ErrorCode.REFUND_NOT_FOUND));
        if (refund.getStatus() == PaymentStatus.SUCCESS) {
            throw new AppException(ErrorCode.REFUND_ALREADY_PROCESSED);
        }
        refund.setStatus(status);
        refund.setUpdatedAt(LocalDateTime.now());
        refundRepository.save(refund);
        return refundMapper.toResponse(refund);
    }

    /**
     * Lấy thông tin chi tiết một yêu cầu hoàn tiền theo ID.
     */
    @Transactional(readOnly = true)
    public RefundResponse getRefundById(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new AppException(ErrorCode.REFUND_NOT_FOUND));
        return refundMapper.toResponse(refund);
    }

    /**
     * Lấy danh sách tất cả các yêu cầu hoàn tiền.
     */
    @Transactional(readOnly = true)
    public List<RefundResponse> listRefunds() {
        return refundRepository.findAll().stream().map(refundMapper::toResponse).toList();
    }
} 