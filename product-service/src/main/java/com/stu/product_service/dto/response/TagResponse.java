//package com.stu.product_service.dto.response;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.stu.product_service.entity.Tag;
//import lombok.Builder;
//import lombok.Data;
//import java.time.LocalDateTime;
//
//@Data
//@Builder
//@JsonInclude(JsonInclude.Include.NON_NULL)
//public class TagResponse {
//    private Long id;
//    private String name;
//    private String description;
//    private Tag.TagStatus status;
//    private Integer usageCount;
//    private Boolean isFeatured;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//}