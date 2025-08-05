package com.stu.product_service.repository;

import com.stu.product_service.dto.request.SearchProductRequest;
import com.stu.product_service.entity.Product;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProductSearchSpecification {

    public Specification<Product> buildSpecification(SearchProductRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 🔍 Keyword search (tên và mô tả)
            if (request.hasKeyword()) {
                String keyword = "%" + request.getKeyword().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), keyword
                );
                Predicate descPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), keyword
                );
                predicates.add(criteriaBuilder.or(namePredicate, descPredicate));
            }

            // 🏷️ Category filter
            if (request.hasCategoryFilter()) {
                if (request.getCategoryId() != null) {
                    predicates.add(criteriaBuilder.equal(
                        root.get("category").get("id"), request.getCategoryId()
                    ));
                } else if (request.getCategory() != null) {
                    predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("category").get("name")),
                        "%" + request.getCategory().toLowerCase() + "%"
                    ));
                }
            }

            // 🏷️ Brand filter
            if (request.hasBrandFilter()) {
                if (request.getBrandId() != null) {
                    predicates.add(criteriaBuilder.equal(
                        root.get("brand").get("id"), request.getBrandId()
                    ));
                } else if (request.getBrand() != null) {
                    predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("brand").get("name")),
                        "%" + request.getBrand().toLowerCase() + "%"
                    ));
                }
            }

            // 💰 Price range filter
            if (request.hasPriceRange()) {
                if (request.getMinPrice() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("price"), request.getMinPrice()
                    ));
                }
                if (request.getMaxPrice() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("price"), request.getMaxPrice()
                    ));
                }
            }

            // 📅 Created date range filter
            if (request.getCreatedFrom() != null || request.getCreatedTo() != null) {
                Path<LocalDateTime> createdAtPath = root.get("createdAt");
                if (request.getCreatedFrom() != null) {
                    LocalDateTime fromDateTime = LocalDateTime.of(
                        request.getCreatedFrom(), LocalTime.MIN
                    );
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        createdAtPath, fromDateTime
                    ));
                }
                if (request.getCreatedTo() != null) {
                    LocalDateTime toDateTime = LocalDateTime.of(
                        request.getCreatedTo(), LocalTime.MAX
                    );
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        createdAtPath, toDateTime
                    ));
                }
            }

            // 📅 Updated date range filter
            if (request.getUpdatedFrom() != null || request.getUpdatedTo() != null) {
                Path<LocalDateTime> updatedAtPath = root.get("updatedAt");
                if (request.getUpdatedFrom() != null) {
                    LocalDateTime fromDateTime = LocalDateTime.of(
                        request.getUpdatedFrom(), LocalTime.MIN
                    );
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        updatedAtPath, fromDateTime
                    ));
                }
                if (request.getUpdatedTo() != null) {
                    LocalDateTime toDateTime = LocalDateTime.of(
                        request.getUpdatedTo(), LocalTime.MAX
                    );
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        updatedAtPath, toDateTime
                    ));
                }
            }

            // 🏷️ Active status filter
            if (request.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("isActive"), request.getIsActive()
                ));
            }

            // 📦 Stock status filter (placeholder - sẽ implement khi có inventory)
            if (request.getStockStatus() != null && !request.getStockStatus().isEmpty()) {
                // TODO: Implement stock status filtering when inventory is available
                // For now, we'll skip this filter
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Specification<Product> addSorting(SearchProductRequest request) {
        return (root, query, criteriaBuilder) -> {
            // Apply sorting
            if ("price".equals(request.getSortBy())) {
                if ("desc".equalsIgnoreCase(request.getSortOrder())) {
                    query.orderBy(criteriaBuilder.desc(root.get("price")));
                } else {
                    query.orderBy(criteriaBuilder.asc(root.get("price")));
                }
            } else if ("createdAt".equals(request.getSortBy())) {
                if ("desc".equalsIgnoreCase(request.getSortOrder())) {
                    query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
                } else {
                    query.orderBy(criteriaBuilder.asc(root.get("createdAt")));
                }
            } else if ("updatedAt".equals(request.getSortBy())) {
                if ("desc".equalsIgnoreCase(request.getSortOrder())) {
                    query.orderBy(criteriaBuilder.desc(root.get("updatedAt")));
                } else {
                    query.orderBy(criteriaBuilder.asc(root.get("updatedAt")));
                }
            } else {
                // Default sort by name
                if ("desc".equalsIgnoreCase(request.getSortOrder())) {
                    query.orderBy(criteriaBuilder.desc(root.get("name")));
                } else {
                    query.orderBy(criteriaBuilder.asc(root.get("name")));
                }
            }
            return null;
        };
    }
} 