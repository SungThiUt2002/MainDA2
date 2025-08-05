//package com.stu.product_service.mapper;
//
//import com.stu.product_service.controller.dtoClient.ProductInfo;
//import com.stu.product_service.entity.ProductVariant;
//import com.stu.product_service.dto.response.ProductVariantResponse;
//import com.stu.product_service.dto.request.CreateProductVariantRequest;
//import com.stu.product_service.dto.request.UpdateProductVariantRequest;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.mapstruct.NullValuePropertyMappingStrategy;
//import org.mapstruct.factory.Mappers;
//import org.mapstruct.MappingTarget;
//import java.util.List;
//
//@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
//public interface ProductVariantMapper {
//
//    @Mapping(target = "productId", ignore = true)
//    ProductVariantResponse toResponse(ProductVariant productVariant);
//
//    @Mapping(target = "variantId", source = "id")
//    @Mapping(target = "productId", source = "product.id")
//    ProductInfo toResponseClient(ProductVariant productVariant);
//
//    List<ProductVariantResponse> toResponseList(List<ProductVariant> productVariants);
//    @Mapping(target = "product", ignore = true)
//    @Mapping(target = "id", ignore = true)
//    ProductVariant toEntity(CreateProductVariantRequest request);
//
//    @Mapping(target = "product", ignore = true)
//    @Mapping(target = "id", ignore = true)
//    void updateProductVariantFromDto(UpdateProductVariantRequest request, @MappingTarget ProductVariant productVariant);
//}