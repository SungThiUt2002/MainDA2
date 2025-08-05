package com.stu.product_service.repository;

import com.stu.product_service.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    
    Optional<Brand> findByName(String name);
    
    List<Brand> findByIsActiveTrue();
    
    @Query("SELECT b FROM Brand b WHERE b.isActive = true ORDER BY b.name")
    List<Brand> findAllActiveBrands();
    
    boolean existsByName(String name);
    
    @Query("SELECT b FROM Brand b WHERE b.name LIKE %:keyword% AND b.isActive = true")
    List<Brand> searchBrandsByName(String keyword);
} 