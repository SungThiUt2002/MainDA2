package com.stu.profile_service.repository;

import com.stu.profile_service.entity.OrderShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderShippingAddressRepository extends JpaRepository<OrderShippingAddress, Long> {
    List<OrderShippingAddress> findByUserId(Long userId);

    Optional<OrderShippingAddress> findByIdAndUserId(Long id, Long userId);
}
