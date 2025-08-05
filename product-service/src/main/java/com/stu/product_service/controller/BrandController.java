package com.stu.product_service.controller;

import com.stu.product_service.dto.request.CreateBrandRequest;
import com.stu.product_service.dto.request.UpdateBrandRequest;
import com.stu.product_service.dto.response.BrandResponse;
import com.stu.product_service.entity.Brand;
import com.stu.product_service.mapper.BrandMapper;
import com.stu.product_service.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {
    private final BrandService brandService;
    private final BrandMapper brandMapper;

    @PostMapping
    public ResponseEntity<BrandResponse> createBrand(@RequestBody @Valid CreateBrandRequest request) {
        var brand = brandService.createBrand(request, 1L); // TODO: lấy createdBy thực tế
        var response = brandMapper.toResponse(brand);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandResponse> updateBrand(@PathVariable Long id, @RequestBody @Valid UpdateBrandRequest request) {
        var brand = brandService.updateBrand(id, request, 1L); // TODO: lấy updatedBy thực tế
        var response = brandMapper.toResponse(brand);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id); // TODO: lấy deletedBy thực tế
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getBrandById(@PathVariable Long id) {
        var brand = brandService.getBrandById(id);
        var response = brandMapper.toResponse(brand);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<BrandResponse>> getAllBrands() {
        var brands = brandService.getAllActiveBrands();
        var responses = brands.stream().map(brandMapper::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<List<BrandResponse>> searchBrands(@RequestParam String keyword) {
        var brands = brandService.searchBrandsByName(keyword);
        var responses = brands.stream().map(brandMapper::toResponse).toList();
        return ResponseEntity.ok(responses);
    }
} 