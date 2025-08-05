package com.stu.account_service.repository;


import com.stu.account_service.entity.OrderShippingAddress;
import com.stu.account_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderShippingAddressRepository extends JpaRepository<OrderShippingAddress, Long> {
    List<OrderShippingAddress> findByUsers(User userId);

    Optional<OrderShippingAddress> findByIdAndUsers(Long id, User userId);
}
