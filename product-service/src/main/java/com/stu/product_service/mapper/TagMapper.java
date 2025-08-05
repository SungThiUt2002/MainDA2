//package com.stu.product_service.mapper;
//
//import com.stu.product_service.entity.Tag;
//import com.stu.product_service.dto.response.TagResponse;
//import com.stu.product_service.dto.request.CreateTagRequest;
//import com.stu.product_service.dto.request.UpdateTagRequest;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.mapstruct.NullValuePropertyMappingStrategy;
//import org.mapstruct.factory.Mappers;
//import org.mapstruct.MappingTarget;
//import java.util.List;
//
//@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
//public interface TagMapper {
//
//    TagResponse toResponse(Tag tag);
//    List<TagResponse> toResponseList(List<Tag> tags);
//    @Mapping(target = "usageCount", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    @Mapping(target = "products", ignore = true)
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "deletedAt", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    Tag toEntity(CreateTagRequest request);
//    @Mapping(target = "usageCount", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    @Mapping(target = "products", ignore = true)
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "deletedAt", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    void updateTagFromDto(UpdateTagRequest request, @MappingTarget Tag tag);
//}