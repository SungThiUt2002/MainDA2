package com.stu.product_service.service;

import com.stu.product_service.entity.Brand;
import com.stu.product_service.repository.BrandRepository;
import com.stu.product_service.exception.ErrorCode;
import com.stu.product_service.exception.ProductServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import com.stu.product_service.dto.request.CreateBrandRequest;
import com.stu.product_service.dto.request.UpdateBrandRequest;
import com.stu.product_service.mapper.BrandMapper;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BrandService {
    
    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;
    
    /**
     * Tạo brand mới
     */
    public Brand createBrand(CreateBrandRequest request, Long createdBy) {

        if (brandRepository.existsByName(request.getName())) {
            throw new ProductServiceException(ErrorCode.BRAND_NAME_EXISTS);
        }
        Brand brand = brandMapper.toEntity(request);
        Brand savedBrand = brandRepository.save(brand);
        log.info("Created brand: {}", savedBrand.getName());
        return savedBrand;
    }
    
    /**
     * Cập nhật brand
     */
    public Brand updateBrand(Long brandId, UpdateBrandRequest request, Long updatedBy) {
        Brand brand = brandRepository.findById(brandId)
            .orElseThrow(() -> new ProductServiceException(ErrorCode.BRAND_NOT_FOUND));

        if (brandRepository.existsByName(request.getName()) && !brand.getName().equals(request.getName())) {
            throw new ProductServiceException(ErrorCode.BRAND_NAME_EXISTS);
        }

        brandMapper.updateBrandFromDto(request, brand);
        Brand updatedBrand = brandRepository.save(brand);
        log.info("Updated brand: {}", updatedBrand.getName());
        return updatedBrand;
    }
    
    /**
     * Xóa brand (soft delete)
     */
    public void deleteBrand(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
            .orElseThrow(() -> new ProductServiceException(ErrorCode.BRAND_NOT_FOUND));

        ///  theme logic, brand không thể xóa nếu có sản phẩm
        brand.setIsActive(false);
        brandRepository.save(brand);
        log.info("Deleted brand: {}", brand.getName());
    }
    
    /**
     * Lấy brand theo ID
     */
    public Brand getBrandById(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
            .orElseThrow(() -> new ProductServiceException(ErrorCode.BRAND_NOT_FOUND));
        return brand;
    }
    
    /**
     * Lấy brand theo tên
     */
    public Optional<Brand> getBrandByName(String name) {
        return brandRepository.findByName(name);
    }
    
    /**
     * Lấy tất cả brand active
     */
    public List<Brand> getAllActiveBrands() {
        return brandRepository.findAll().stream().filter(Brand::getIsActive).toList();
    }
    
    /**
     * Tìm kiếm brand theo tên
     */
    public List<Brand> searchBrandsByName(String keyword) {
        return brandRepository.searchBrandsByName(keyword);
    }
    
    /**
     * Kiểm tra brand có tồn tại không
     */
    public boolean existsByName(String name) {
        return brandRepository.existsByName(name);
    }
    
    /**
     * Lấy tất cả brand (bao gồm inactive)
     */
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }
} 