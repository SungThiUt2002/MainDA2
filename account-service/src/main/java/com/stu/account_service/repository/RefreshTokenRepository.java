package com.stu.account_service.repository;

import com.stu.account_service.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByJti(String jti); // tìm token theo id

    RefreshToken save(RefreshToken refreshToken);
// ... existing code ...

    boolean existsByJti(String jti); // check thời gian  hết hạn của token

    Optional<RefreshToken> findByTokenHash(String tokenHash); // timf token theo mã hash

    // tìm tất cả token của user
    List<RefreshToken> findByUserId(Long userId);

    void deleteByExpiresAtBefore(LocalDateTime expiryDate);

    // Thêm method mới để lấy refresh token đang hoạt động
    List<RefreshToken> findByUserIdAndRevokedFalse(Long userId);


}
