package com.stu.product_service.mapper;

import com.stu.product_service.entity.ProductImage;
import com.stu.product_service.dto.response.ProductImageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {

    @Mapping(target = "productId", source = "product.id")
    ProductImageResponse toResponse(ProductImage productImage);
    List<ProductImageResponse> toResponseList(List<ProductImage> productImages);
} 