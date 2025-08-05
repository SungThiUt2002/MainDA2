package com.stu.account_service.controller;


import com.stu.account_service.dto.request.OrderShippingAddressRequest;
import com.stu.account_service.dto.response.OrderShippingAddressResponse;
import com.stu.account_service.entity.OrderShippingAddress;
import com.stu.account_service.mapper.OrderShippingAddressMapper;
import com.stu.account_service.service.OrderShippingAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class OrderShippingAddressController {
    private  final OrderShippingAddressService service;
    private final OrderShippingAddressMapper mapper;

    // Tạo địa chỉ mới
    @PostMapping
    public OrderShippingAddressResponse createAddress(@RequestBody OrderShippingAddressRequest request,
                                                      @RequestHeader("Authorization") String authorizationHeader)
    {
        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
        var address = service.createAddress(request,token);
        var response = mapper.toResponse(address);
        return  response;
    }

    // getAddressesByUserId
    @GetMapping("/all/{userId}")
    public List<OrderShippingAddressResponse> getAddressesByUserId(@PathVariable Long userId){
        List<OrderShippingAddress> address = service.getAddressesByUserId(userId);
        List<OrderShippingAddressResponse> response = mapper.toResponseList(address);
        return response;
    }

    @GetMapping("/{id}/users/{userId}")
    public OrderShippingAddressResponse getOnAddress(@PathVariable Long id, @PathVariable Long userId){
        var address = service.getAddressById(id,userId);
        OrderShippingAddressResponse response = mapper.toResponse(address);
        return  response;
    }

}
