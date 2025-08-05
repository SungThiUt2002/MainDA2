package com.stu.payment_service.mapper;

import com.stu.payment_service.entity.Payment;
import com.stu.payment_service.dto.request.CreatePaymentRequest;
import com.stu.payment_service.dto.response.PaymentResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentResponse toResponse(Payment payment);
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "refunds", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Payment toEntity(CreatePaymentRequest request);
} 