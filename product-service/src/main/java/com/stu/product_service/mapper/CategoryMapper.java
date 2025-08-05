package com.stu.product_service.mapper;

import com.stu.product_service.entity.Category;
import com.stu.product_service.dto.response.CategoryResponse;
import com.stu.product_service.dto.request.CreateCategoryRequest;
import com.stu.product_service.dto.request.UpdateCategoryRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import org.mapstruct.MappingTarget;
import java.util.List;

@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);
    List<CategoryResponse> toResponseList(List<Category> categories);

    @Mapping(target = "products", ignore = true)
    @Mapping(target = "id", ignore = true)
    Category toEntity(CreateCategoryRequest request);
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateCategoryFromDto(UpdateCategoryRequest request, @MappingTarget Category category);
} 