package com.stu.payment_service.service;

import com.stu.common_dto.event.PaymentEvent.PaymentFailedEvent;
import com.stu.common_dto.event.PaymentEvent.PaymentSuccessEvent;
import com.stu.payment_service.dto.request.CreatePaymentRequest;
import com.stu.payment_service.dto.response.PaymentResponse;
import com.stu.payment_service.entity.Payment;
import com.stu.payment_service.entity.PaymentTransaction;
import com.stu.payment_service.enums.PaymentStatus;
import com.stu.payment_service.enums.TransactionAction;
import com.stu.payment_service.event.producer.PaymentProducer;
import com.stu.payment_service.exception.AppException;
import com.stu.payment_service.exception.ErrorCode;
import com.stu.payment_service.mapper.PaymentMapper;
import com.stu.payment_service.repository.PaymentRepository;
import com.stu.payment_service.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentProducer  paymentProducer;

    // 1. Tạo một hóa đơn thanh toán mới
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {

        if (paymentRepository.existsByOrderId(request.getOrderId())){
            throw new AppException(ErrorCode.PAYMENT_ALREADY_EXISTS, "Đơn hàng đã có giao dịch thanh toán.");
        }

        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new AppException(ErrorCode.PAYMENT_AMOUNT_INVALID);
        }
        Payment payment = paymentMapper.toEntity(request);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }


    // 2. Xác nhận trạng thái thanh toán (thành công/thất bại/hủy) và lưu transaction tương ứng.(giả lập)
    @Transactional
    public PaymentResponse confirmPayment(String orderId, PaymentStatus status, String gatewayResponse) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        if (payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_CONFIRMED);
        }

        payment.setStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
        // Lưu transaction
        paymentTransactionRepository.save(PaymentTransaction.builder()
                .payment(payment)
                .action(status == PaymentStatus.SUCCESS ? TransactionAction.PAY : TransactionAction.CANCEL)
                .status(status)
                .gatewayResponse(gatewayResponse)
                .createdAt(LocalDateTime.now())
                .build());

        // Thanh toán thành công
        if (status == PaymentStatus.SUCCESS){
            // gửi event về cho order service
            PaymentSuccessEvent event= PaymentSuccessEvent.builder()
                    .orderId(payment.getOrderId())
                    .paymentId(payment.getId())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .paidAt(LocalDateTime.now())
                    .build();
            paymentProducer.sendPaymentSuccess(event);
        }else if(status == PaymentStatus.FAILED){
            // Gửi event thất bại về OrderService
            PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                    .orderId(payment.getOrderId())
                    .userId(payment.getUserId())
                    .reason("Thanh toán thất bại hoặc bị huỷ")
                    .failedAt(LocalDateTime.now())
                    .build();
            paymentProducer.sendPaymentFailed(failedEvent);
        }

        return paymentMapper.toResponse(payment);
    }


    /**
     * Lấy thông tin chi tiết một thanh toán theo ID.
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        return paymentMapper.toResponse(payment);
    }

    /**
     * Lấy danh sách tất cả các thanh toán.
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> listPayments() {
        return paymentRepository.findAll().stream().map(paymentMapper::toResponse).toList();
    }

    /**
     * Hủy một thanh toán nếu chưa xác nhận thành công.
     */
    @Transactional
    public void cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_CONFIRMED);
        }
        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    /**
     * Lấy lịch sử các transaction của một thanh toán.
     */
    @Transactional(readOnly = true)
    public List<PaymentTransaction> getPaymentTransactions(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        return payment.getTransactions();
    }
} 