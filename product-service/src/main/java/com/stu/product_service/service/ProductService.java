package com.stu.product_service.service;

import com.stu.common_dto.event.ProductEvent.ProductCreatEvent;
import com.stu.common_dto.event.ProductEvent.ProductDeleteEvent;
import com.stu.common_dto.event.ProductEvent.ProductUpdateEvent;
import com.stu.product_service.dto.request.CreateProductRequest;
import com.stu.product_service.dto.request.SearchProductRequest;
import com.stu.product_service.dto.request.UpdateProductRequest;
import com.stu.product_service.entity.*;
import com.stu.product_service.event.producers.ProductEventPublisher;
import com.stu.product_service.exception.ErrorCode;
import com.stu.product_service.exception.ProductServiceException;
import com.stu.product_service.mapper.ProductMapper;
import com.stu.product_service.repository.CategoryRepository;
import com.stu.product_service.repository.ProductRepository;
import com.stu.product_service.repository.ProductSearchSpecification;
import com.stu.product_service.repository.BrandRepository;

import com.stu.product_service.security.JwtUtil;
import com.stu.product_service.entity.TokenBlacklistCache;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;
    private final ProductEventPublisher productEventPublisher;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistCache tokenBlacklistCache;
    private final ProductSearchSpecification productSearchSpecification;

    // 1. tạo sản phẩm (admin)
    public Product createProduct(CreateProductRequest request, String token) {

        // 0. Xác thực token
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        if (jwtUtil.isTokenExpired(token)) {
            throw new ProductServiceException(ErrorCode.UNAUTHENTICATED, "Token hết hạn");
        }

        // Kiểm tra token blacklist
        String jti = jwtUtil.extractJti(token);
        if (tokenBlacklistCache.contains(jti)) {
            throw new ProductServiceException(ErrorCode.UNAUTHENTICATED, "Token đã bị thu hồi");
        }

        //1. kiểm tra xem tên sản phẩm đã tồn tại chưa --> đảm bảo tên không trùng nhau
        if (productRepository.existsByName(request.getName()))
            throw new ProductServiceException(ErrorCode.PRODUCT_ALREADY_EXISTS);

        // 2. Kiểm tra xem id danh mục có tồn tại không (đảm bảo dữ liệu đầu vào)
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ProductServiceException(ErrorCode.CATEGORY_NOT_FOUND));

        // 3.Brand có thể để trống (nếu không thuộc brand nào)
        // Nếu brand mà không trống thì phải đảm bảo brand đã tồn tại
        Brand brand = null;
        if (request.getBrandId() != null) {
            brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ProductServiceException(ErrorCode.BRAND_NOT_FOUND));
        }
        // Lưu vào dữ liệu
        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        product.setBrand(brand);
        product.setCreatedAt(LocalDateTime.now());
        Product saved = productRepository.save(product);

        // gửi event, hoặc gọi api để tạo sản phẩm trong kho tương ứng
        ProductCreatEvent event = new ProductCreatEvent();
            event.setProductId(saved.getId());
            event.setProductName(saved.getName());
        productEventPublisher.sendProductCreateEvent(event);

        return saved;
    }

    //2. update sản phẩm (admin)
    // update theo id
    public Product updateProduct(Long id, UpdateProductRequest request,String token) {

        //0. Xác thực token
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        if (jwtUtil.isTokenExpired(token)) {
            throw new ProductServiceException(ErrorCode.UNAUTHENTICATED, "Token hết hạn");
        }

        // 1.Kiểm tra token blacklist
        String jti = jwtUtil.extractJti(token);
        if (tokenBlacklistCache.contains(jti)) {
            throw new ProductServiceException(ErrorCode.UNAUTHENTICATED, "Token đã bị thu hồi");
        }

        Long userId = jwtUtil.extractUserId(token);

        //2. kiểm tra xem id sản phẩm đã tồn tại chưa, nếu chưa đưa ra ngoại lệ và không thể update
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductServiceException(ErrorCode.PRODUCT_NOT_FOUND));
        productMapper.updateProductFromDto(request, product);

        // 3.update tên sản phẩm(nếu có)
        if (request.getName() != null)
        {
            product.setName(request.getName());
        }

        // 4. update giá sản phẩm (nếu có)
        if (request.getPrice() != null){
            product.setPrice(request.getPrice());
        }

        // 5. update danh mục của sản phẩm
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ProductServiceException(ErrorCode.CATEGORY_NOT_FOUND));
            product.setCategory(category);
        }

        // 6. update brand của sản phẩm
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ProductServiceException(ErrorCode.BRAND_NOT_FOUND));
            product.setBrand(brand);
        }

        // 7. Lưu kết quả
        productMapper.updateProductFromDto(request,product); // dùng mapper
        product.setUpdatedAt(LocalDateTime.now());
        Product updated = productRepository.save(product);

        // 8. gửi event hoặc api để update sản phẩm trong kho, trong giỏ hàng
        ProductUpdateEvent event = new ProductUpdateEvent();
            event.setProductId(id);
            event.setProductName(updated.getName());
            event.setNewPrice(updated.getPrice());
            event.setIsActive(updated.getIsActive());
            productEventPublisher.sendProductUpdateEvent(event);
        return updated;
    }
    

    //3. xóa sản phẩm (admin)
    @Transactional
    public void deleteProduct(Long productId) {

        // 1. Kiểm tra sản phẩm đã tồn tại chưa --> nếu chưa không thể xóa
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductServiceException(ErrorCode.PRODUCT_NOT_FOUND));

        // 2.Xóa product
        productRepository.deleteById(productId);

        // Xóa ảnh của product tương ứng

        // 3. Gửi event đẻ xóa sản phẩm trong kho, giỏ hàng
        ProductDeleteEvent productEvent = ProductDeleteEvent.builder()
                .productId(productId)
                .reason("Product deleted")
                .build();
        productEventPublisher.sendProductDeleteEvent(productEvent);
    }

    // 4.lấy toàn bộ sản phẩm
    public List<Product> getAllProducts() {
        try {
            return productRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ProductServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi lấy danh sách sản phẩm: " + e.getMessage());
        }
    }

    //5. lấy tất cả sản phẩm theo danh mục
    public List<Product> getProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ProductServiceException(ErrorCode.CATEGORY_NOT_FOUND));
        List<Product> products = productRepository.findByCategoryId(categoryId);
        return products;
    }

    //6. hiện chi tiết một sản
    public Product getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductServiceException(ErrorCode.PRODUCT_NOT_FOUND));
        return product;
    }

    // Lọc product theo Brand
    public List<Product> getProductByBrand( Long brandId){
        Brand brand=  brandRepository.findById(brandId)
                .orElseThrow(()-> new ProductServiceException(ErrorCode.BRAND_NOT_FOUND));

        List<Product> products = productRepository.findByBrandId(brandId);
        return products;
    }

    // ==================== 🔍 ADVANCED SEARCH & FILTER METHODS ====================

    /**
     * 🔍 Tìm kiếm sản phẩm với advanced filters và pagination
     */
    public Page<Product> searchProducts(SearchProductRequest request) {
        try {
            // Tạo specification cho search và filter
            Specification<Product> searchSpec = productSearchSpecification.buildSpecification(request);
            Specification<Product> sortSpec = productSearchSpecification.addSorting(request);
            
            // Combine specifications
            Specification<Product> combinedSpec = Specification.where(searchSpec).and(sortSpec);
            
            // Tạo Pageable cho pagination
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            
            // Thực hiện search với pagination
            return productRepository.findAll(combinedSpec, pageable);
            
        } catch (Exception e) {
            throw new ProductServiceException(ErrorCode.INTERNAL_SERVER_ERROR, 
                "Lỗi khi tìm kiếm sản phẩm: " + e.getMessage());
        }
    }

    /**
     * 📊 Lấy danh sách categories cho filter dropdown
     */
    public List<String> getDistinctCategories() {
        try {
            return productRepository.findDistinctCategoryNames();
        } catch (Exception e) {
            throw new ProductServiceException(ErrorCode.INTERNAL_SERVER_ERROR, 
                "Lỗi khi lấy danh sách categories: " + e.getMessage());
        }
    }

    /**
     * 📊 Lấy danh sách brands cho filter dropdown
     */
    public List<String> getDistinctBrands() {
        try {
            return productRepository.findDistinctBrandNames();
        } catch (Exception e) {
            throw new ProductServiceException(ErrorCode.INTERNAL_SERVER_ERROR, 
                "Lỗi khi lấy danh sách brands: " + e.getMessage());
        }
    }

    /**
     * 💰 Lấy khoảng giá min-max cho price range filter
     */
    public Map<String, BigDecimal> getPriceRange() {
        try {
            Object[] result = productRepository.getPriceRange();
            Map<String, BigDecimal> priceRange = new HashMap<>();
            
            if (result != null && result.length >= 2) {
                priceRange.put("minPrice", (BigDecimal) result[0]);
                priceRange.put("maxPrice", (BigDecimal) result[1]);
            } else {
                priceRange.put("minPrice", BigDecimal.ZERO);
                priceRange.put("maxPrice", BigDecimal.ZERO);
            }
            
            return priceRange;
        } catch (Exception e) {
            throw new ProductServiceException(ErrorCode.INTERNAL_SERVER_ERROR, 
                "Lỗi khi lấy khoảng giá: " + e.getMessage());
        }
    }

    /**
     * 🔍 Tìm kiếm đơn giản theo keyword
     */
    public List<Product> searchByKeyword(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllProducts();
            }
            return productRepository.searchByKeyword(keyword.trim());
        } catch (Exception e) {
            throw new ProductServiceException(ErrorCode.INTERNAL_SERVER_ERROR, 
                "Lỗi khi tìm kiếm theo keyword: " + e.getMessage());
        }
    }

        /**
     * 📊 Đếm tổng số lượng sản phẩm
     */
    public Long getTotalProductCount() {
        try {
            return productRepository.count();
        } catch (Exception e) {
            throw new ProductServiceException(ErrorCode.INTERNAL_SERVER_ERROR, 
                "Lỗi khi đếm tổng số sản phẩm: " + e.getMessage());
        }
    }

} 