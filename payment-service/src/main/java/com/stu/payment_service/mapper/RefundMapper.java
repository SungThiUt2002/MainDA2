package com.stu.payment_service.mapper;

import com.stu.payment_service.entity.Refund;
import com.stu.payment_service.dto.request.CreateRefundRequest;
import com.stu.payment_service.dto.response.RefundResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RefundMapper {
    @Mapping(target = "paymentId", ignore = true)
    RefundResponse toResponse(Refund refund);

    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "payment", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Refund toEntity(CreateRefundRequest request);
} 