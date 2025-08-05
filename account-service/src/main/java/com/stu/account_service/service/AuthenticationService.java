package com.stu.account_service.service;

import com.stu.account_service.config.JwtUtil;
import com.stu.account_service.dto.request.LoginRequest;
import com.stu.account_service.dto.request.RefreshTokenRequest;
import com.stu.account_service.dto.request.SessionInfo;
import com.stu.account_service.dto.response.AuthenticationResponse;
import com.stu.account_service.entity.BlacklistToken;
import com.stu.account_service.entity.RefreshToken;
import com.stu.account_service.entity.User;
import com.stu.account_service.event.TokenBlacklistPublisher;
import com.stu.account_service.exception.AppException;
import com.stu.account_service.exception.ErrorCode;
import com.stu.account_service.repository.BlacklistTokenRepository;
import com.stu.account_service.repository.RefreshTokenRepository;
import com.stu.account_service.repository.RoleRepository;
import com.stu.account_service.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationService {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final BlacklistTokenRepository blacklistTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistPublisher tokenBlacklistPublisher;

    public AuthenticationResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        log.info("Authenticating user: {}", request.getUsernameOrEmail());

        try {
            // 1. Tìm user theo username hoặc email
            User user = userRepository.findByUsername(request.getUsernameOrEmail())
                    .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            // Kiểm tra mật khẩu
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new AppException(ErrorCode.INVALID_CREDENTIALS);
            }

            // 3. Kiểm tra trạng thái tài khoản
            if (!user.isEnabled() || !user.isAccountNonLocked()) {
                throw new AppException(ErrorCode.ACCOUNT_DISABLED);
            }

            // 4. Ghi lại đăng nhập thành công (bỏ logic loginAttemptService)

            // 5. Sinh access token và refresh token
            String accessToken = jwtUtil.generateToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);
            Long expiresIn = jwtUtil.getJwtExpiration();

            // lấy id user để lưu vào entity
            User userRefresh = new User();
            userRefresh.setId(user.getId());

            RefreshToken refreshTokenEntity = RefreshToken.builder()
                    .jti(jwtUtil.extractJti(refreshToken))
                    .tokenHash(jwtUtil.hashToken(refreshToken))
                    .user(userRefresh)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtUtil.getRefreshExpiration())))
                    .revoked(false)
                    .build();
            refreshTokenRepository.save(refreshTokenEntity);

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(expiresIn)
                    .build();

            // đăng nhập thằng công
            // lưu lần đăng nhập gần nahats vào user


        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            // Ghi lại đăng nhập thất bại (bỏ logic loginAttemptService)
            log.error("Login failed for user: {}", request.getUsernameOrEmail(), e);
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
    }



    // logout
    public void logout(String accessToken, String refreshToken) {

        // 1.Xử lý access token
        if (accessToken == null || accessToken.isBlank()) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        // Tự động cắt "Bearer " nếu có, còn không thì dùng luôn
        String jwtAccessToken = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        try {
            // lấy id token
            Long userId = jwtUtil.extractUserId(jwtAccessToken);

            // Lấy đầy đủ User từ DB để xác thực
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            // kiểm tra xem access token hơp lệ không
            boolean valid = jwtUtil.isTokenValid(jwtAccessToken,user, blacklistTokenRepository);
            if (!valid) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }

            User userRefresh = new User();
            userRefresh.setId(user.getId());

            // Blacklist access token
            String hashToken = jwtUtil.hashToken(jwtAccessToken);
            // kiểm tra xem nếu chưa đăng xuất --> lưu
            if (!blacklistTokenRepository.existsByToken(hashToken)) {
                BlacklistToken blacklist = BlacklistToken.builder()
                        .token(hashToken)
                        .user(userRefresh)
                        .expiryDate(LocalDateTime.now().plus(Duration.ofMillis(jwtUtil.getRefreshExpiration())))
                        .createdAt(LocalDateTime.now())
                        .build();
                blacklistTokenRepository.save(blacklist);

                // Sau khi lưu blacklistTokenRepository.save(blacklist);
                // gửi sự kiện logout ể đồng bộ với  các service khác
                long ttlSeconds = Duration.between(LocalDateTime.now(), blacklist.getExpiryDate()).getSeconds();
                String jti = jwtUtil.extractJti(jwtAccessToken);
                tokenBlacklistPublisher.publishBlacklistEvent(jti, ttlSeconds);
                log.info("Published blacklist event for JTI: {}", jti);
            }

            // 2. Kiểm tra refresh token khi logout
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN, "Refresh token is required for logout");
            }
            // Lấy id refresh token
            String refreshJti = jwtUtil.extractJti(refreshToken);
            RefreshToken refreshTokenEntity = refreshTokenRepository.findByJti(refreshJti)
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

            // xác thực refresh token
            boolean validRefresh = jwtUtil.isRefreshTokenValid(refreshToken,user, refreshTokenRepository);
            if (!validRefresh) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }


            // Không cho phép dùng refresh token đã bị thu hồi hoặc đã được dùng để cấp token mới
            if (refreshTokenEntity.isRevoked() || refreshTokenEntity.getUsedAt() != null) {
                throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN, "Refresh token đã được dùng hoặc đã bị thu hồi");
            }

            refreshTokenEntity.setRevoked(true);
            refreshTokenEntity.setUsedAt(LocalDateTime.now());
            refreshTokenRepository.save(refreshTokenEntity);

            log.info("User with ID {} logged out successfully", userId);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.LOGOUT_FAILED);
        }
    }



    @Transactional
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        String oldRefreshToken = request.getRefreshToken();

        String username = jwtUtil.extractUsername(oldRefreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Kiểm tra refresh token hợp lệ (chưa bị revoke, chưa dùng, đúng user, chưa hết hạn)
        boolean valid = jwtUtil.isRefreshTokenValid(oldRefreshToken, user, refreshTokenRepository);
        if (!valid) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        // Thu hồi refresh token cũ (one-time use)
        String idOldRefreshToken = jwtUtil.extractJti(oldRefreshToken);
        RefreshToken oldToken = refreshTokenRepository.findByJti(idOldRefreshToken)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));
        oldToken.setRevoked(true);
        oldToken.setUsedAt(LocalDateTime.now());
        refreshTokenRepository.save(oldToken);

        // Tạo access token & refresh token mới
        String newAccessToken = jwtUtil.generateToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        // Lưu refresh token mới
        RefreshToken refreshToken = RefreshToken.builder()
                .jti(jwtUtil.extractJti(newRefreshToken))
                .tokenHash(jwtUtil.hashToken(newRefreshToken))
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtUtil.getRefreshExpiration())))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtUtil.getJwtExpiration())
                .build();
    }

    // Tự động dọn dẹp refresh token hết hạn
    @Scheduled(cron = "${security.login.cleanup-cron}")
    public void cleanupExpiredLoginAttempts() {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Đã xóa các bản ghi refresh token hết hạn");
    }

    // tự động dọn dẹp blacklist token hết hạn
    @Scheduled(cron = "${security.login.cleanup-cron}")
    public void cleanupExpiredBlacklistTokens() {
        blacklistTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
        log.info("Đã xóa các BlacklistToken hết hạn");
    }
}