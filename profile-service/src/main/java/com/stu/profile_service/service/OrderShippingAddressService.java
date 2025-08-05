package com.stu.profile_service.service;

import com.stu.profile_service.dto.request.OrderShippingAddressRequest;
import com.stu.profile_service.dto.response.OrderShippingAddressResponse;
import com.stu.profile_service.entity.OrderShippingAddress;
import com.stu.profile_service.exception.AppException;
import com.stu.profile_service.exception.ErrorCode;
import com.stu.profile_service.mapper.OrderShippingAddressMapper;
import com.stu.profile_service.repository.OrderShippingAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderShippingAddressService {
    private final OrderShippingAddressRepository orderShippingAddressRepository;

    public OrderShippingAddress createAddress(OrderShippingAddressRequest request) {

        OrderShippingAddress address = OrderShippingAddress.builder()
                .userId(request.getUserId())
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
        return orderShippingAddressRepository.findByUserId(userId);
    }

    // Lấy mọt địa chỉ theo id and userId
    public OrderShippingAddress getAddressById(Long id, Long userId) {
        OrderShippingAddress address = orderShippingAddressRepository.findByIdAndUserId(id,userId)
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
