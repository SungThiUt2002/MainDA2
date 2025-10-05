package com.stu.product_service.mapper;

import com.stu.product_service.dto.response.BrandResponse;
import com.stu.product_service.entity.Brand;
import com.stu.product_service.dto.request.CreateBrandRequest;
import com.stu.product_service.dto.request.UpdateBrandRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BrandMapper {

    BrandResponse toResponse(Brand brand);

    @Mapping(target = "products", ignore = true)
    @Mapping(target = "isActive", expression = "java(request.getStatus() != null && request.getStatus().equals(\"ACTIVE\"))")
    @Mapping(target = "id", ignore = true)
    Brand toEntity(CreateBrandRequest request);

    @Mapping(target = "products", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateBrandFromDto(UpdateBrandRequest request, @MappingTarget Brand brand);
} 