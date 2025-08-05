package com.stu.product_service.controller;

import com.stu.product_service.dto.request.CreateProductRequest;
import com.stu.product_service.dto.request.SearchProductRequest;
import com.stu.product_service.dto.request.UpdateProductRequest;
import com.stu.product_service.dto.response.ApiResponse;
import com.stu.product_service.dto.response.ProductResponse;
import com.stu.product_service.entity.Product;
import com.stu.product_service.service.ProductService;
import com.stu.product_service.mapper.ProductMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.stu.product_service.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid CreateProductRequest request,
                                                         @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
        var product = productService.createProduct(request,token);
        var response = productMapper.toAResponse(product);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @RequestBody UpdateProductRequest request,
                                                         @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
        var product = productService.updateProduct(id, request,token);
        var response = productMapper.toAResponse(product);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Xóa sản phẩm thành công");
    }

    // lấy 01 sản phẩm theo id
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        var product = productService.getProductById(id);
        var response = productMapper.toAResponse(product);
        return ResponseEntity.ok(response);
    }

    // Toàn bọ sản phẩm
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        var products = productService.getAllProducts();
        var responses = productMapper.toResponseList(products);
        return ResponseEntity.ok(responses);
    }

    // Lấy sản phẩm theo categoryId
    @GetMapping("/byCategory/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable Long categoryId) {
        var products = productService.getProductsByCategory(categoryId);
        var responses = productMapper.toResponseList(products);
        return ResponseEntity.ok(responses);
    }

    // Lấy sản phẩm theo brand
    @GetMapping("/brands/{brandId}")
    public ResponseEntity<List<ProductResponse>> getProductByBrand( @PathVariable Long brandId){
        var products = productService.getProductByBrand(brandId);
        var response = productMapper.toResponseList(products);
        return ResponseEntity.ok(response);
    }

    // ==================== 🔍 ADVANCED SEARCH & FILTER ENDPOINTS ====================

    /**
     * 🔍 Tìm kiếm sản phẩm với advanced filters và pagination
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(@ModelAttribute SearchProductRequest request) {
        log.info("🔍 Search request: {}", request);
        
        try {
            Page<Product> productsPage = productService.searchProducts(request);
            Page<ProductResponse> responsePage = productsPage.map(productMapper::toAResponse);
            
            log.info("✅ Search completed. Found {} products", productsPage.getTotalElements());
            return ResponseEntity.ok(responsePage);
            
        } catch (Exception e) {
            log.error("❌ Search error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 🔍 Tìm kiếm đơn giản theo keyword
     */
    @GetMapping("/search/keyword")
    public ResponseEntity<List<ProductResponse>> searchByKeyword(@RequestParam String keyword) {
        log.info("🔍 Keyword search: {}", keyword);
        
        try {
            List<Product> products = productService.searchByKeyword(keyword);
            List<ProductResponse> responses = productMapper.toResponseList(products);
            
            log.info("✅ Keyword search completed. Found {} products", products.size());
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            log.error("❌ Keyword search error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 📊 Lấy danh sách categories cho filter dropdown
     */
    @GetMapping("/filters/categories")
    public ResponseEntity<List<String>> getDistinctCategories() {
        log.info("📊 Getting distinct categories");
        
        try {
            List<String> categories = productService.getDistinctCategories();
            log.info("✅ Found {} distinct categories", categories.size());
            return ResponseEntity.ok(categories);
            
        } catch (Exception e) {
            log.error("❌ Error getting categories: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 📊 Lấy danh sách brands cho filter dropdown
     */
    @GetMapping("/filters/brands")
    public ResponseEntity<List<String>> getDistinctBrands() {
        log.info("📊 Getting distinct brands");
        
        try {
            List<String> brands = productService.getDistinctBrands();
            log.info("✅ Found {} distinct brands", brands.size());
            return ResponseEntity.ok(brands);
            
        } catch (Exception e) {
            log.error("❌ Error getting brands: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 💰 Lấy khoảng giá min-max cho price range filter
     */
    @GetMapping("/filters/price-range")
    public ResponseEntity<Map<String, BigDecimal>> getPriceRange() {
        log.info("💰 Getting price range");
        
        try {
            Map<String, BigDecimal> priceRange = productService.getPriceRange();
            log.info("✅ Price range: {}", priceRange);
            return ResponseEntity.ok(priceRange);
            
        } catch (Exception e) {
            log.error("❌ Error getting price range: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 📊 Lấy tổng số lượng sản phẩm cho dashboard
     */
    @GetMapping("/stats/count")
    public ResponseEntity<Long> getTotalProductCount() {
        log.info("📊 Getting total product count");
        
        try {
            Long totalCount = productService.getTotalProductCount();
            log.info("✅ Total product count: {}", totalCount);
            return ResponseEntity.ok(totalCount);
            
        } catch (Exception e) {
            log.error("❌ Error getting total product count: {}", e.getMessage(), e);
            throw e;
        }
    }

} 