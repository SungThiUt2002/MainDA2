package com.stu.account_service.repository;

import com.stu.account_service.entity.BlacklistToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BlacklistTokenRepository extends JpaRepository<BlacklistToken, Long> {

    boolean existsByToken(String tokenHash);
    List<BlacklistToken> findByUserId(Long userId); // Tìm tất cả các blacklisst token  của user
    void deleteByExpiryDateBefore(LocalDateTime expiryDate);
}
