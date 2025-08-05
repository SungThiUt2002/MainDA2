package com.stu.product_service.repository;

import com.stu.product_service.entity.Category;
import com.stu.product_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    boolean existsByName(String name);
    Optional<Product> findByName(String name);
    void deleteByName( String name);
    List<Product> findByCategoryId(Long categoryId); // tìm sản phẩm theo danh mục
    List<Product> findByBrandId(Long categoryId);
    
    // 🔍 Search methods
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByKeyword(@Param("keyword") String keyword);
    
    // 📊 Get distinct categories for filter dropdown
    @Query("SELECT DISTINCT c.name FROM Product p JOIN p.category c ORDER BY c.name")
    List<String> findDistinctCategoryNames();
    
    // 📊 Get distinct brands for filter dropdown
    @Query("SELECT DISTINCT b.name FROM Product p JOIN p.brand b ORDER BY b.name")
    List<String> findDistinctBrandNames();
    
        // 💰 Get price range for filter
    @Query("SELECT MIN(p.price), MAX(p.price) FROM Product p")
    Object[] getPriceRange();
} 