package com.stu.order_service.service;

import com.stu.common_dto.enums.PaymentMethod;

import com.stu.common_dto.enums.ShippingStatus;
import com.stu.common_dto.event.InventoryEvent.InventoryOrderEvent;
import com.stu.common_dto.event.InventoryEvent.InventoryOrderItem;
import com.stu.common_dto.event.OrderEvent.OrderCreatedEvent;
import com.stu.common_dto.event.PaymentEvent.PaymentFailedEvent;
import com.stu.common_dto.event.PaymentEvent.PaymentSuccessEvent;
import com.stu.common_dto.event.ShippingEvent.ShippingRequestEvent;
import com.stu.order_service.client.AddressClient;
import com.stu.order_service.client.InventoryClient;
import com.stu.order_service.client.dto.OrderShippingAddressResponse;
import com.stu.order_service.dto.request.*;
import com.stu.order_service.dto.response.OrderResponse;
import com.stu.order_service.entity.Order;
import com.stu.order_service.entity.OrderItem;
import com.stu.order_service.entity.OrderStatusHistory;
import com.stu.order_service.enums.OrderStatus;
import com.stu.order_service.event.producers.CreateOrderProducer;
import com.stu.order_service.event.producers.InventoryOrderProducer;

import com.stu.order_service.exception.AppException;
import com.stu.order_service.exception.ErrorCode;
import com.stu.order_service.mapper.OrderItemMapper;
import com.stu.order_service.mapper.OrderMapper;
import com.stu.order_service.repository.OrderRepository;
import com.stu.order_service.repository.OrderStatusHistoryRepository;
import com.stu.order_service.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final InventoryClient inventoryClient; // Giả lập client tích hợp Inventory Service// Giả lập client tích hợp Product Service
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private  final AddressClient addressClient;
    private final InventoryOrderProducer inventoryProducer;
    private final CreateOrderProducer createOrderProducer;
    private final JwtUtil jwtUtil;

//    public Order getOneOrderById( String orderId, String token){
//        Order order= orderRepository.findById(orderId)
//                .orElseThrow(()-> new AppException(ErrorCode.ORDER_NOT_FOUND));
//
//        Long userIdToken =
//
//    }

    // internal - cart service(cart checkout listener)
    @Transactional
    public Order createOrder(CreateOrderRequest request) {

        // 1. Validate input
        if (request.getUserId() == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_ORDER_REQUEST, "UserId and items are required");
        }

        // 2. Kiểm tra tồn kho qua Inventory Service
        for (OrderItemRequest item : request.getItems()) {
            Integer available = inventoryClient.getAvailableQuantity(item.getProductId());
            if (available == null || available < item.getQuantity()) {
                throw new AppException(ErrorCode.OUT_OF_STOCK, "Product" + item.getProductId() + " is out of stock");
            }
        }

        // 3. Tạo order
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (OrderItemRequest itemReq : request.getItems()) {
            BigDecimal itemTotal = itemReq.getProductPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .productId(itemReq.getProductId())
                    .productPrice(itemReq.getProductPrice())
                    .productName(itemReq.getProductName())
                    .quantity(itemReq.getQuantity())
                    .totalPrice(itemTotal)
                    .build();
            orderItems.add(orderItem);
        }
        // dùng mapper
        Order order = orderMapper.toOrder(request);;
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(totalAmount);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setOrderItems(orderItems);

        // 5. Gán order cho từng orderItem
        orderItems.forEach(item -> item.setOrder(order));

        // 6. Lưu vào DB
        Order saved = orderRepository.save(order);

        //7. Lưu vào Lịch sử
        OrderStatusHistory orderStatusHistory = OrderStatusHistory.builder()
                .order(order)
                .status(order.getStatus())
                .changedAt(LocalDateTime.now())
                .build();
        orderStatusHistoryRepository.save(orderStatusHistory);

        return saved;
    }

    // Lựa chọn địa chỉ, phương thức thanh toán cho đơn hàng (đã được tạo)
    @Transactional
    public Order updateOrderInfo(String orderId, UpdateOrderInfoRequest request){
        // Kiểm tra xem order đã tồn tại chưa
         Order order = orderRepository.findById(orderId)
                 .orElseThrow(()->new AppException(ErrorCode.ORDER_NOT_FOUND));

         // Đảm bảo rằng trạng thái của đơn hàng là create (chưa thanh tóan, chưa xác nhận)
        if (order.getStatus() != OrderStatus.CREATED){
            throw new AppException(ErrorCode.ORDER_ALREADY_CONFIRM);
        }

        // Chọn lấy địa chỉ giao hàng từ profile service(gọi api)
        OrderShippingAddressResponse address = addressClient.getOnAddress(request.getAddressId(),order.getUserId());
            order.setReceiverName(address.getReceiverName());
            order.setReceiverPhone(address.getReceiverPhone());
            order.setProvince(address.getProvince());
            order.setDistrict(address.getDistrict());
            order.setWard(address.getWard());
            order.setStreetAddress(address.getStreetAddress());
            order.setAddressId(request.getAddressId());

        // Chọn phương thức thanh toán, ghi chú
        order.setPaymentMethod(request.getPaymentMethod());
        order.setNote(request.getNote());

        // Ghi lịch sử hủy
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(order.getStatus())
                .changedAt(LocalDateTime.now())
                .build();
        orderStatusHistoryRepository.save(history);

        // Lưu thông tin
        return orderRepository.save(order);
    }

    // Xác nhận tạo đơn hàng đảm bảo đủ các thông tin khi USER ấn đặt hàng
    @Transactional
    public void confirmCreateOrder( String orderId){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getPaymentMethod() == null) {
            throw new AppException(ErrorCode.ORDER_INFO_INCOMPLETE, "Chưa chọn phương thức thanh toán");
        }

        if ( order.getReceiverName() == null || order.getReceiverPhone() == null || order.getStreetAddress() == null) {
            throw new AppException(ErrorCode.ORDER_INFO_INCOMPLETE,"Chưa có địa chỉ giao hàng");
        }

//        if (order.getPaymentMethod() == PaymentMethod.COD) {
            // COD: chờ Admin xác nhận thủ công
            order.setStatus(OrderStatus.PENDING_CONFIRMATION);

            // Ghi lịch sử hủy
            OrderStatusHistory history = OrderStatusHistory.builder()
                    .order(order)
                    .status(order.getStatus())
                    .changedAt(LocalDateTime.now())
                    .build();
            orderStatusHistoryRepository.save(history);

            // gủi event cho kho giữ hàng
            List<InventoryOrderItem> reservedItems = order.getOrderItems().stream()
                    .map(item -> InventoryOrderItem.builder()
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .build())
                    .toList();

            InventoryOrderEvent reserveEvent = InventoryOrderEvent.builder()
                    .orderId(order.getId())
                    .userId(order.getUserId())
                    .createdAt(LocalDateTime.now())
                    .items(reservedItems)
                    .build();
            inventoryProducer.sendInventoryReserveEvent(reserveEvent);

//        } else {
//            // Thanh toán online → Chờ thanh toán
//            order.setStatus(OrderStatus.PENDING_PAYMENT);
//
//            // Ghi lịch sử hủy
//            OrderStatusHistory history = OrderStatusHistory.builder()
//                    .order(order)
//                    .status(order.getStatus())
//                    .changedAt(LocalDateTime.now())
//                    .build();
//            orderStatusHistoryRepository.save(history);
//
//            // Gửi event cho kho giữu hàng
//            List<InventoryOrderItem> reservedItems = order.getOrderItems().stream()
//                    .map(item -> InventoryOrderItem.builder()
//                            .productId(item.getProductId())
//                            .quantity(item.getQuantity())
//                            .build())
//                    .toList();
//
//            InventoryOrderEvent reserveEvent = InventoryOrderEvent.builder()
//                    .orderId(order.getId())
//                    .userId(order.getUserId())
//                    .createdAt(LocalDateTime.now())
//                    .items(reservedItems)
//                    .build();
//            inventoryProducer.sendInventoryReserveEvent(reserveEvent);
//
//            // Gửi event cho payment service hận event để thanh toán
//            OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
//                    .orderId(order.getId())
//                    .userId(order.getUserId())
//                    .totalAmount(order.getTotalAmount())
//                    .createdAt(LocalDateTime.now())
//                    .paymentMethod(order.getPaymentMethod()) // Bổ sung dòng này để truyền paymentMethod
//                    .build();
//            createOrderProducer.sendCreateOrderEvent(orderCreatedEvent);
//        };
        // Lưu kết quả
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

    }

    // Hàm xác nhận đơn hàng bởi AMDIN cho đơn hàng thanh toán bằng COD(thanh toán khi nhận hàng)
    @Transactional
    public void confirmOrderByAdmin(String orderId){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() != OrderStatus.PENDING_CONFIRMATION) {
            throw new AppException(ErrorCode.ORDER_STATUS_INVALID); //Chỉ đơn hàng chờ xác nhận mới được xác nhận
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Ghi lịch sử hủy
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(OrderStatus.CONFIRMED)
                .changedAt(LocalDateTime.now())
                .build();
        orderStatusHistoryRepository.save(history);

        // 1. Gửi event cho kho để trừ kho ( chuyển từ đặt trước --> SALE)
        List<InventoryOrderItem> reservedItems = order.getOrderItems().stream()
                .map(item -> InventoryOrderItem.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .toList();
        InventoryOrderEvent reserveEvent = InventoryOrderEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .createdAt(LocalDateTime.now())
                .items(reservedItems)
                .build();
        inventoryProducer.sendSellReservedStock(reserveEvent);

        // 2. Gửi event cho shipping service để vận chuyển
        // gộp địa chỉ lại để gửi đi
//        String shippingAddress = String.join(", ",
//                    order.getStreetAddress(),
//                    order.getWard(),
//                    order.getDistrict(),
//                    order.getProvince()
//            );
//        ShippingRequestEvent  event = new ShippingRequestEvent();
//                event.setOrderId(order.getId());
//                event.setUserId(order.getUserId());
//                event.setReceiverName(order.getReceiverName());
//                event.setReceiverPhone(order.getReceiverPhone());
//                event.setShippingAddress(shippingAddress);
//                event.setPaymentMethod(order.getPaymentMethod());
//                event.setNote(order.getNote());
//                event.setPaidAt(order.getUpdatedAt());
//
//            // Gửi event cho Shipping Service
//            shipingProducer.sendShippingRequest(event);
    }


//    // Xử lý cập nhập trạng thái sau khi thanh toán xong(internal - payment listener)
//    @Transactional
//    public void handlePaymentSuccess(PaymentSuccessEvent event) {
//        Order order = orderRepository.findById(event.getOrderId())
//                .orElseThrow(() -> new RuntimeException("Order not found"));
//        // 1. Set trạng thái đơn hàng khi thanh toán thành công
//        order.setStatus(OrderStatus.CONFIRMED); // thanh toán thành công mới được xác nhận đơn hàng
//        order.setPaymentId(event.getPaymentId());
//        order.setUpdatedAt(LocalDateTime.now());
//        orderRepository.save(order);
//
//        // 2. Lưu vào lịch sử
//        OrderStatusHistory orderStatusHistory = OrderStatusHistory.builder()
//                    .order(order)
//                    .status(order.getStatus())
//                    .changedAt(order.getUpdatedAt())
//                    .build();
//        orderStatusHistoryRepository.save(orderStatusHistory);
//
//        // 3. Gửi event cho Inventory Service để chuyển sản phẩm đã đặtruowcscs --> sell
//        List<InventoryOrderItem> reservedItems = order.getOrderItems().stream()
//                .map(item -> InventoryOrderItem.builder()
//                        .productId(item.getProductId())
//                        .quantity(item.getQuantity())
//                        .build())
//                .toList();
//
//        InventoryOrderEvent reserveEvent = InventoryOrderEvent.builder()
//                .orderId(order.getId())
//                .userId(order.getUserId())
//                .createdAt(LocalDateTime.now())
//                .items(reservedItems)
//                .build();
//
//        inventoryProducer.sendSellReservedStock(reserveEvent);
//
//
////        ShippingRequestEvent shipping = new ShippingRequestEvent();
////        shipping.setOrderId(order.getId());
////        shipping.setUserId(order.getUserId());
////        shipping.setReceiverName(order.getReceiverName());
////        shipping.setReceiverPhone(order.getReceiverPhone());
////        shipping.setShippingAddress(shippingAddress);
////        shipping.setPaymentMethod(order.getPaymentMethod());
////        shipping.setNote(order.getNote());
////        shipping.setPaidAt(order.getUpdatedAt());
////        shipingProducer.sendShippingRequest(shipping);
//    }

//    // Xử lý khi thanh  toán thất bại
//    @Transactional
//    public void handlePaymentFailed(PaymentFailedEvent event){
//        log.info("Received PaymentFailedEvent: {}", event);
//        // 1. Kiểm tra xem order này có tồn tại không
//        Order order = orderRepository.findById(event.getOrderId())
//                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
//
//        order.setStatus(OrderStatus.PAYMENT_FAILED); // thanh toán thất baiij hoặc hủy thành toán
//        order.setUpdatedAt(LocalDateTime.now());
//        orderRepository.save(order);
//
//        // 2. lưu lịch sử đơn hàng
//        OrderStatusHistory orderStatusHistory = OrderStatusHistory.builder()
//                .order(order)
//                .status(order.getStatus())
//                .changedAt(order.getUpdatedAt())
//                .build();
//        orderStatusHistoryRepository.save(orderStatusHistory);
//
//        // 3. Gửi event cho Inventory Service để chuyển sản phẩm đã đặtruowcscs --> release
//        List<InventoryOrderItem> reservedItems = order.getOrderItems().stream()
//                .map(item -> InventoryOrderItem.builder()
//                        .productId(item.getProductId())
//                        .quantity(item.getQuantity())
//                        .build())
//                .toList();
//
//        InventoryOrderEvent reserveEvent = InventoryOrderEvent.builder()
//                .orderId(order.getId())
//                .userId(order.getUserId())
//                .createdAt(LocalDateTime.now())
//                .items(reservedItems)
//                .build();
//        inventoryProducer.sendPaymentFailForStock(reserveEvent);
//    }

//    // Xử lý cập nhập trạng thái theo trạng thái của đơn hàng(interal)
//    @Transactional
//    public void handleShippingDelivered( String orderId  , ShippingStatusRequest request){
//
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new RuntimeException("Order not found"));
//
//        if (order.getStatus() == OrderStatus.DELIVERY_SUCCESSFUL ) {
//            throw new AppException(ErrorCode.ORDER_ALREADY_DELIVERED);
//        }
//
//        if (order.getStatus() == OrderStatus.CANCELLED) {
//            throw new AppException(ErrorCode.ORDER_ALREADY_CANCELLED);
//        }
//
//        order.setShippingId(request.getShippingOrderId());
//        order.setStatus(request.getNewStatus());
//        order.setUpdatedAt(request.getUpdatedAt());
//        order.setNote(request.getDescription());
//        orderRepository.save(order);
//
//        // Lưu vào lich sử
//        OrderStatusHistory orderStatusHistory = OrderStatusHistory.builder()
//                .order(order)
//                .status(order.getStatus())
//                .changedAt(order.getUpdatedAt())
//                .build();
//        orderStatusHistoryRepository.save(orderStatusHistory);
//
//    }

    // URER
    @Transactional
    public void cancelOrder(String orderId, String token) {
        Long userIdToken = jwtUtil.extractUserId(token);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Kiểm tra quyền sở hữu - chỉ cho phép user hủy đơn hàng của chính mình
        if (!order.getUserId().equals(userIdToken)) {
            throw new AppException(ErrorCode.PERMISSION_DENIED, "Bạn không có quyền hủy đơn hàng này");
        }

        if (order.getStatus() == OrderStatus.DELIVERING || order.getStatus() == OrderStatus.DELIVERY_SUCCESSFUL) {
            throw new AppException(ErrorCode.ORDER_CANNOT_BE_CANCELLED);
        }

        List<InventoryOrderItem> inventoryItems = order.getOrderItems().stream()
                .map(item -> InventoryOrderItem.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        InventoryOrderEvent inventoryEvent = InventoryOrderEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .items(inventoryItems)
                .createdAt(LocalDateTime.now())
                .build();

        switch (order.getStatus()) {
            case CREATED, CONFIRM_INFORMATION -> {
                // Đơn hàng mới tạo hoặc đang điền thông tin - không cần xử lý inventory
                // Chỉ cần hủy đơn hàng
            }
            case PENDING_PAYMENT, PENDING_CONFIRMATION -> {
                inventoryProducer.sendPaymentFailForStock(inventoryEvent);
            }
            case CONFIRMED -> {
                inventoryProducer.sendReturnStockEvent(inventoryEvent);
            }
            default -> {
                throw new AppException(ErrorCode.ORDER_CANNOT_BE_CANCELLED,
                        "Không hỗ trợ hủy đơn ở trạng thái: " + order.getStatus());
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Ghi lịch sử hủy
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(OrderStatus.CANCELLED)
                .changedAt(LocalDateTime.now())
                .build();
        orderStatusHistoryRepository.save(history);
    }

    // Cập nhập trạng thái vận chuyển (admin)
    // Thực tế là do bên gioa hàng cập nhập
    // Yêu cầu
    @Transactional
    public Order updateShippingStatus(String orderId, UpdateSatusShippingRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = request.getNewStatus();

        if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.RETURNED) {
            throw new AppException(ErrorCode.ORDER_ALREADY_FINALIZED);
        }

        if (currentStatus != OrderStatus.CONFIRMED && currentStatus != OrderStatus.DELIVERING && currentStatus != OrderStatus.DELIVERY_SUCCESSFUL) {
            throw new AppException(ErrorCode.ORDER_NOT_CONFIRM);
        }

        if (!isValidTransition(currentStatus, newStatus)) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION);
        }

        order.setStatus(newStatus);
        order.setNote(request.getDescription());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Lưu vào lịch sử
        orderStatusHistoryRepository.save(
                OrderStatusHistory.builder()
                        .order(order)
                        .status(newStatus)
                        .changedAt(LocalDateTime.now())
                        .build()
        );

        // Gửi event cho kho dựa vào Statu
        List<InventoryOrderItem> inventoryItems = order.getOrderItems().stream()
                .map(item -> InventoryOrderItem.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        InventoryOrderEvent inventoryEvent = InventoryOrderEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .items(inventoryItems)
                .createdAt(LocalDateTime.now())
                .build();

        if (order.getStatus() == OrderStatus.DELIVERY_SUCCESSFUL){

            inventoryProducer.sendOrderSuccessful(inventoryEvent);
        }else if (order.getStatus() == OrderStatus.RETURNED){
            inventoryProducer.sendReturnStockEvent(inventoryEvent);
        }

        return order;
    }

    // Kiểm tra chuyển đổi status hợp lệ hay không
    private boolean isValidTransition(OrderStatus current, OrderStatus next) {
        return switch (current) {
            case CONFIRMED -> Set.of(OrderStatus.DELIVERING).contains(next);
            case DELIVERING -> Set.of(OrderStatus.DELIVERY_SUCCESSFUL, OrderStatus.RETURNED).contains(next);
            case DELIVERY_SUCCESSFUL -> Set.of(OrderStatus.RETURNED).contains(next);
            default -> false;
        };
    }


    //============USER ======================
    //====================================================================================//
    // Lấy đơn hàng mới nhất của user
    public OrderResponse getLatestOrder(String token) {

        Long userIdToken = jwtUtil.extractUserId(token);
        log.info("Getting latest order for user ID: {}", userIdToken);
        
        Order latestOrder = orderRepository.findTopByUserIdOrderByCreatedAtDesc(userIdToken)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND, "Không tìm thấy đơn hàng"));

        log.info("Found latest order: ID={}, Status={} for user {}", latestOrder.getId(), latestOrder.getStatus(), userIdToken);
        return orderMapper.toOrderResponse(latestOrder);
    }

    // Lấy tất cả đơn hàng của user (cho lịch sử đơn hàng)
    public List<OrderResponse> getUserOrders(String token) {
        Long userIdToken = jwtUtil.extractUserId(token);
        log.info("Getting all orders for user ID: {}", userIdToken);

        List<Order> userOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(userIdToken);

        log.info("Found {} orders for user {}", userOrders.size(), userIdToken);
        return userOrders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    // Lấy đơn hàng của user theo trạng thái
    public List<OrderResponse> getUserOrdersByStatus(String token, OrderStatus status) {
        Long userIdToken = jwtUtil.extractUserId(token);
        log.info("Getting orders for user ID: {} with status: {}", userIdToken, status);

        List<Order> userOrders = orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userIdToken, status);

        log.info("Found {} orders for user {} with status {}", userOrders.size(), userIdToken, status);
        return userOrders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    // ========== ADMIN FUNCTIONS ================================================================

    // Lấy tất cả đơn hàng chờ xác nhận (PENDING_CONFIRMATION) - cho Admin
    public List<OrderResponse> getPendingConfirmationOrders() {
        log.info("Getting all pending confirmation orders for admin");
        
        List<Order> pendingOrders = orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.PENDING_CONFIRMATION);
        
        log.info("Found {} pending confirmation orders", pendingOrders.size());
        return pendingOrders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    // Lấy chi tiết đơn hàng theo ID - cho Admin
    public OrderResponse getOrderById(String orderId) {
        log.info("Getting order details for admin, order ID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND, "Không tìm thấy đơn hàng"));
        
        log.info("Found order: ID={}, Status={}, User={}", order.getId(), order.getStatus(), order.getUserId());
        return orderMapper.toOrderResponse(order);
    }

    // Lấy tất cả đơn hàng theo trạng thái - cho Admin
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        log.info("Getting orders by status for admin: {}", status);
        
        List<Order> orders = orderRepository.findByStatusOrderByCreatedAtDesc(status);
        
        log.info("Found {} orders with status {}", orders.size(), status);
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    // Lấy tất cả đơn hàng - cho Admin
    public List<OrderResponse> getAllOrders() {
        log.info("Getting all orders for admin");
        
        List<Order> allOrders = orderRepository.findAll();
        
        log.info("Found {} total orders", allOrders.size());
        return allOrders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    // Đếm tổng số đơn hàng - cho Admin Dashboard
    public Long getTotalOrderCount() {
        log.info("Getting total order count for admin dashboard");
        Long totalCount = orderRepository.count();
        log.info("Total order count: {}", totalCount);
        return totalCount;
    }


    // USER
    // Lấy tất cả đơn hàng của user


}

