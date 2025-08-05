package com.stu.account_service.service;
import com.stu.account_service.config.JwtUtil;
import com.stu.account_service.dto.request.OrderShippingAddressRequest;
import com.stu.account_service.entity.OrderShippingAddress;
import com.stu.account_service.entity.User;
import com.stu.account_service.exception.AppException;
import com.stu.account_service.exception.ErrorCode;
import com.stu.account_service.repository.OrderShippingAddressRepository;
import com.stu.account_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderShippingAddressService {
    private final OrderShippingAddressRepository orderShippingAddressRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public OrderShippingAddress createAddress(OrderShippingAddressRequest request, String token) {

        Long userIdToken = jwtUtil.extractUserId(token);
        User user = new User();
        user.setId(userIdToken);
        OrderShippingAddress address = OrderShippingAddress.builder()
                .users(user)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .province(request.getProvince())
                .district(request.getDistrict())
                .ward(request.getWard())
                .streetAddress(request.getStreetAddress())
                .build();
        return orderShippingAddressRepository.save(address);
    }

    // Lấy tất cả địa chỉ theo userId
    public List<OrderShippingAddress> getAddressesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//        user.setId(userId);
        return orderShippingAddressRepository.findByUsers(user);
    }

    // Lấy mọt địa chỉ theo id and userId
    public OrderShippingAddress getAddressById(Long id, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        OrderShippingAddress address = orderShippingAddressRepository.findByIdAndUsers(id,user)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        return  address;
    }
//
//    public void deleteAddress(Long id) {
//        if (!repository.existsById(id)) {
//            throw new AppException(ErrorCode.ADDRESS_NOT_FOUND, "Address not found with id: " + id);
//        }
//        repository.deleteById(id);
//    }
//
//    public OrderShippingAddress updateAddress(Long id, OrderShippingAddressRequest request) {
//        OrderShippingAddress address = repository.findById(id)
//                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND, "Address not found with id: " + id));
//        address.setShippingAddress(request.getShippingAddress());
//        address.setReceiverName(request.getReceiverName());
//        address.setReceiverPhone(request.getReceiverPhone());
//        return repository.save(address);
//    }
}
