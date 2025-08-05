package com.stu.account_service.repository;

import com.stu.account_service.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    Optional<Role> findById(Long id);
    boolean existsById( Long id);
    boolean existsByName(String name);

}
