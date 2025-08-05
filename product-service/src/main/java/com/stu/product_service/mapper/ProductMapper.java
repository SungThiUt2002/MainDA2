package com.stu.product_service.mapper;

import com.stu.product_service.controller.dtoClient.ProductInfo;
import com.stu.product_service.dto.request.CreateProductRequest;
import com.stu.product_service.dto.request.UpdateProductRequest;
import com.stu.product_service.dto.response.*;
import com.stu.product_service.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {


    @Mapping(target = "images", source = "images")
    ProductResponse toAResponse(Product product);

    @Mapping(target = "productName", source = "name")
    @Mapping(target = "productId", source = "id")
    ProductInfo toResponseClient(Product product);

    List<ProductResponse> toResponseList(List<Product> products);

    @Mapping(target = "productId", source = "product.id")
    ProductImageResponse productImageToProductImageResponse(ProductImage image);


    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    Product toEntity(CreateProductRequest request);


    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    void updateProductFromDto(UpdateProductRequest request, @MappingTarget Product product);
} 