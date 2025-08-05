package com.stu.product_service.repository;

import com.stu.product_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
//    List<BlacklistToken> findByUserId(Long userId);
    Optional<Category> findByName(String name);
    void deleteByName( String name);
} 