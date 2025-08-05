package com.stu.product_service.controller;

import com.stu.product_service.dto.request.CreateCategoryRequest;
import com.stu.product_service.dto.request.UpdateCategoryRequest;
import com.stu.product_service.dto.response.ApiResponse;
import com.stu.product_service.dto.response.CategoryResponse;

import com.stu.product_service.service.CategoryService;
import com.stu.product_service.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CreateCategoryRequest request) {
        var category = categoryService.createCategory(request);
        var response = categoryMapper.toResponse(category);
        return ResponseEntity.ok(response);
    }

    // update danh mục
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable  Long id, @RequestBody UpdateCategoryRequest request) {
        var category = categoryService.updateCategory(id, request);
        var response = categoryMapper.toResponse(category);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // lấy danh mục theo tên
    @GetMapping("/{name}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable String name) {
        var category = categoryService.getCategory(name);
        var response = categoryMapper.toResponse(category);
        return ResponseEntity.ok(response);
    }

    // lấy toàn bộ danh mục
    @GetMapping("/allCategory")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        var categories = categoryService.getAllCategories();
        var responses = categoryMapper.toResponseList(categories);
        return ResponseEntity.ok(responses);
    }
} 