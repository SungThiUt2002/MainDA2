package com.stu.shipping_service.service;

import com.stu.common_dto.event.ShippingEvent.ShippingStatusEvent;
import com.stu.shipping_service.dto.request.CreateShippingOrderRequest;
import com.stu.shipping_service.dto.request.UpdateSatusShippingRequest;
import com.stu.shipping_service.dto.response.ShippingOrderResponse;
import com.stu.shipping_service.dto.response.ShippingStatusHistoryResponse;
import com.stu.shipping_service.entity.ShippingOrder;
import com.stu.shipping_service.entity.ShippingStatusHistory;
import com.stu.common_dto.enums.ShippingStatus;
import com.stu.shipping_service.event.producer.ShippingStatusProducer;
import com.stu.shipping_service.exception.ErrorCode;
import com.stu.shipping_service.exception.ShippingException;
import com.stu.shipping_service.mapper.ShippingOrderMapper;
import com.stu.shipping_service.mapper.ShippingStatusHistoryMapper;
import com.stu.shipping_service.repository.ShippingOrderRepository;
import com.stu.shipping_service.repository.ShippingStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShippingService {
    private final ShippingOrderRepository shippingOrderRepository;
    private final ShippingStatusHistoryRepository shippingStatusHistoryRepository;
    private final ShippingOrderMapper shippingOrderMapper;
    private final ShippingStatusHistoryMapper shippingStatusHistoryMapper;
    private final ShippingStatusProducer shippingStatusProducer;


    // 1. Hàm tạo mã vận chuyển (thông thường là tích hợp với bên vận chuyển thứ 3, do bên vận chuyển cung cấp)
    private String generateTrackingCode(String orderId) {
        return "SHIP-" + orderId + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    // 2. tạo mới đơn vận chuyển (Tự động tạo khi nhận event)
    @Transactional
    public ShippingOrderResponse createShippingOrder(CreateShippingOrderRequest request) {

        // Kiểm tra nếu đơn vận chuyển đã tồn tại cho đơn hàng này
        if (shippingOrderRepository.existsByOrderId(request.getOrderId())) {
            throw new ShippingException(ErrorCode.SHIPPING_ORDER_EXISTED);
        }

        ShippingOrder order = shippingOrderMapper.toEntity(request);
        order.setOrderId(request.getOrderId());
        order.setStatus(ShippingStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());
        order.setTrackingCode(generateTrackingCode(request.getOrderId()));
        order.setShippingFee(BigDecimal.valueOf(20000)); /// để mặc định sau này sửa sau
        ShippingOrder saved = shippingOrderRepository.save(order);
//        private String orderId;
//        private String shippingAddress;
//        private String receiverName;
//        private String receiverPhone;
//        private String shippingMethod;
////    private String trackingCode;
////    private BigDecimal shippingFee;
//        private String note;

        // Lưu lịch sử trạng thái đầu tiên
        ShippingStatusHistory history = ShippingStatusHistory.builder()
                .shippingOrder(saved)
                .status(ShippingStatus.CREATED)
                .changedAt(LocalDateTime.now())
                .description("Đơn vận chuyển được tạo mới")
                .build();
        shippingStatusHistoryRepository.save(history);
        return shippingOrderMapper.toResponse(saved);
    }

    /**(giả lập)
     * Cập nhật trạng thái đơn vận chuyển và lưu lịch sử trạng thái(ADMIN)
     * Thực tế là do bên vận chuyển thứ 3, vận chuyển thành công thì sẽ cập nhập bên thứ 3, và hệ thộng tự đông cập nhập
     */
    @Transactional
    public ShippingOrderResponse updateShippingStatus(String orderId, UpdateSatusShippingRequest request) {

        ShippingOrder shipping = shippingOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ShippingException(ErrorCode.SHIPPING_ORDER_NOT_FOUND));

        // Nếu đã giao hàng thành công / hoàn hàng thì không thể cập nhập trạng thái đơn hàng được nữa
        if (shipping.getStatus() == ShippingStatus.DELIVERY_SUCCESSFUL || shipping.getStatus() == ShippingStatus.RETURNED) {
            throw new ShippingException(ErrorCode.SHIPPING_ALREADY_DELIVERED);
        }

        shipping.setStatus(request.getNewStatus());
        shipping.setUpdatedAt(LocalDateTime.now());
        shippingOrderRepository.save(shipping);

        // Lưu lịch sử trạng thái
        ShippingStatusHistory history = ShippingStatusHistory.builder()
                .shippingOrder(shipping)
                .status(request.getNewStatus())
                .changedAt(LocalDateTime.now())
                .description(request.getDescription())
                .build();
        shippingStatusHistoryRepository.save(history);

        // gửi event cập nhập trạng thái sau khi luu
        ShippingStatusEvent event= ShippingStatusEvent.builder()
                .orderId(shipping.getOrderId())
                .shippingOrderId(shipping.getId())
                .newStatus(shipping.getStatus())
                .updatedAt(shipping.getUpdatedAt())
                .description(request.getDescription())
                .build();
        shippingStatusProducer.sendShippingStatuss(event);

        return shippingOrderMapper.toResponse(shipping);
    }

    /**
     * Lấy chi tiết một đơn vận chuyển.
     */
    @Transactional(readOnly = true)
    public ShippingOrderResponse getShippingOrderById(Long id) {
        ShippingOrder order = shippingOrderRepository.findById(id)
                .orElseThrow(() -> new ShippingException(ErrorCode.SHIPPING_ORDER_NOT_FOUND));
        return shippingOrderMapper.toResponse(order);
    }

    /**
     * Lấy danh sách tất cả đơn vận chuyển.
     */
    @Transactional(readOnly = true)
    public List<ShippingOrderResponse> listShippingOrders() {
        return shippingOrderRepository.findAll().stream()
                .map(shippingOrderMapper::toResponse)
                .collect(Collectors.toList());
    }


     //Lấy lịch sử giao hàng của một đơn hàng
    @Transactional(readOnly = true)
    public List<ShippingStatusHistoryResponse> getShippingStatusHistory(Long shippingOrderId) {
        ShippingOrder order = shippingOrderRepository.findById(shippingOrderId)
                .orElseThrow(() -> new ShippingException(ErrorCode.SHIPPING_ORDER_NOT_FOUND));
        return order.getStatusHistory().stream()
                .map(shippingStatusHistoryMapper::toResponse)
                .collect(Collectors.toList());
    }
} 