package com.stu.cart_service.mapper;

import com.stu.cart_service.dto.request.CartRequest;
import com.stu.cart_service.dto.response.CartResponse;
import com.stu.cart_service.entity.Cart;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", uses = {CartItemMapper.class})
public interface CartMapper {


    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Cart toEntity(CartRequest request);

    CartResponse toResponse(Cart entity);

    List<CartResponse> toResponseList(List<Cart> entities);
}
