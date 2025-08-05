//package com.stu.product_service.dto.response;
//
//import lombok.Builder;
//import lombok.Data;
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Set;
//
//@Data
//@Builder
//public class ProductAdminResponse {
//    private Long id;
//    private String name;
//    private String description;
//    private BigDecimal price;
//    private String status;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//    private Long createdBy;
//    private Long updatedBy;
//    private LocalDateTime deletedAt;
//    private Long deletedBy;
//    private CategoryResponse category;
//    private List<ProductImageResponse> images;
//    private List<ProductVariantResponse> variants;
//    private Set<TagResponse> tags;
//    private Long categoryId;
//    private Long brandId;
//    private String barcode;
//    private String slug;
//    private Boolean isFeatured;
//    private Boolean isActive;
//    private String attributes;
////    ;
////    private List<Long> imageIds;
////    private List<Long> variantIds;
////    private Set<Long> tagIds;
//}