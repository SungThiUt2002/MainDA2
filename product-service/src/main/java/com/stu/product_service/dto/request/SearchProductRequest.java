package com.stu.product_service.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SearchProductRequest {
    
    // 🔍 Search keywords
    private String keyword;           // Tìm theo tên, mô tả
    
    // 🏷️ Category & Brand filters
    private String category;          // Lọc theo category name
    private String brand;             // Lọc theo brand name
    private Long categoryId;          // Lọc theo category ID
    private Long brandId;             // Lọc theo brand ID
    
    // 💰 Price range filter
    private BigDecimal minPrice;      // Giá tối thiểu
    private BigDecimal maxPrice;      // Giá tối đa
    
    // 📦 Stock status filter
    private String stockStatus;       // Trạng thái tồn kho (in_stock, out_of_stock, low_stock)
    
    // 📅 Date range filters
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdFrom;    // Ngày tạo từ
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdTo;      // Ngày tạo đến
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate updatedFrom;    // Ngày cập nhật từ
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate updatedTo;      // Ngày cập nhật đến
    
    // 🏷️ Status filter
    private Boolean isActive;         // Trạng thái active/inactive
    
    // 📊 Sorting
    private String sortBy = "name";   // Sắp xếp theo: name, price, createdAt, updatedAt
    private String sortOrder = "asc"; // Thứ tự: asc, desc
    
    // 📄 Pagination
    private int page = 0;             // Trang hiện tại (0-based)
    private int size = 20;            // Số item/trang
    
    // 🔧 Helper methods
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
    
    public boolean hasPriceRange() {
        return minPrice != null || maxPrice != null;
    }
    
    public boolean hasDateRange() {
        return createdFrom != null || createdTo != null || updatedFrom != null || updatedTo != null;
    }
    
    public boolean hasCategoryFilter() {
        return category != null || categoryId != null;
    }
    
    public boolean hasBrandFilter() {
        return brand != null || brandId != null;
    }
} 