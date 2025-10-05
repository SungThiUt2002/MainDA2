package com.stu.product_service.service;

import com.stu.product_service.dto.request.CreateCategoryRequest;
import com.stu.product_service.dto.request.UpdateCategoryRequest;
import com.stu.product_service.entity.Category;
import com.stu.product_service.repository.CategoryRepository;
import com.stu.product_service.mapper.CategoryMapper;
import com.stu.product_service.exception.ProductServiceException;
import com.stu.product_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // Tạo mới danh mục sản phẩm
    public Category createCategory(CreateCategoryRequest request) {
        try {
            // Kiểm tra sơ bộ để tránh unnecessary database calls
            if (categoryRepository.existsByName(request.getName()))
                throw new ProductServiceException(ErrorCode.CATEGORY_ALREADY_EXISTS);
            
            Category category = categoryMapper.toEntity(request);
            return categoryRepository.save(category);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Handle duplicate key constraint violation at database level
            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("UKt8o6pivur7nn124jehx7cygw5")) {
                throw new ProductServiceException(ErrorCode.CATEGORY_ALREADY_EXISTS);
            }
            throw e; // Re-throw other database integrity violations
        }
    }

    // update danh mục theo id
    public Category updateCategory(Long id, UpdateCategoryRequest request) {
        //Tìm xem id có tồn tại không, nếu không không thể cập nhập
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ProductServiceException(ErrorCode.CATEGORY_NOT_FOUND));
        // nếu id tồn tại --> tiến hành cập nhập
        categoryMapper.updateCategoryFromDto(request, category);
        // lưu lại kết quả cập nhập
        return categoryRepository.save(category);
    }

    // Xóa danh mục theo theo id
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ProductServiceException(ErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.deleteById(id);

        // danh mục không thể xóa nếu có sản phẩm
    }

    // lấy dnah mục theo id hoặc theo tên
    public Category getCategory(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new ProductServiceException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    // lấy toàn bộ danh mục
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
