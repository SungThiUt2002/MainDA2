package com.stu.account_service.config;

import com.stu.account_service.entity.Permission;
import com.stu.account_service.entity.RefreshToken;
import com.stu.account_service.entity.Role;
import com.stu.account_service.entity.User;
import com.stu.account_service.exception.AppException;
import com.stu.account_service.exception.ErrorCode;
import com.stu.account_service.repository.BlacklistTokenRepository;
import com.stu.account_service.repository.RefreshTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

//    private final String secret = "yourSecretKeyWithAtLeast32Characters!"; // Nên >=32 ký tự
//    private final SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "access");
        return buildToken(claims, user, jwtExpiration);
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "refresh");
        return buildToken(claims, user, refreshExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, User user, long expiration) {
        extraClaims.put("userId", user.getId());

        if ("access".equals(extraClaims.get("tokenType"))) {
            extraClaims.put("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
            extraClaims.put("permissions", user.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(Permission::getName)
                    .collect(Collectors.toList()));
        }

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .setId(generateJti())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateJti() {
        return UUID.randomUUID().toString();
    }

    public boolean isTokenValid(String token, User user, BlacklistTokenRepository blacklistRepo) {
        try {
            if (!isTokenContentValid(token, user)) {
                log.debug("❌ Token content invalid: username or userId mismatch");
                return false;
            }

            if (isTokenExpired(token)) {
                log.debug("❌ Token expired at: {}", extractExpiration(token));
                return false;
            }

            if (!isUserAccountValid(user)) {
                log.debug("❌ User account invalid: {}", user.getUsername());
                return false;
            }

            if (isTokenBlacklisted(token, blacklistRepo)) {
                log.debug("❌ Token is blacklisted: {}", hashToken(token));
                return false;
            }

            if (!isJtiPresent(token)) {
                log.debug("❌ Missing JTI in token");
                return false;
            }

            log.debug("✅ Token is valid for user: {}", user.getUsername());
            return true;

        } catch (Exception e) {
            log.error("❌ Token validation failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /*
         Hàm kiểm tra một refresh token có hợp lệ hay không bằng cách:
        Giải mã token để lấy các thông tin (claims).
        Kiểm tra token có phải loại "refresh" không.
        Kiểm tra token có đúng user không (username và userId khớp).
        Kiểm tra token chưa hết hạn.
        Kiểm tra tài khoản user còn hợp lệ (chưa bị khóa/vô hiệu).
        Kiểm tra trong database:
        Token tồn tại.
        Chưa bị thu hồi (isRevoked = false).
        Chưa được sử dụng (usedAt = null).
        👉 Nếu tất cả điều kiện đều đúng → trả về true. Ngược lại → false.
     */
    public boolean isRefreshTokenValid(String token, User user, RefreshTokenRepository repo) {
        try {
            Claims claims = extractAllClaims(token);

            if (!"refresh".equals(claims.get("tokenType", String.class))) {
                log.debug("❌ Token is not a refresh token");
                return false;
            }

            if (!Objects.equals(claims.getSubject(), user.getUsername())
                    || !Objects.equals(claims.get("userId", Long.class), user.getId())) {
                log.debug("❌ Refresh token user mismatch");
                return false;
            }

            if (claims.getExpiration().before(new Date())) {
                log.debug("❌ Refresh token expired at: {}", claims.getExpiration());
                return false;
            }

            if (!isUserAccountValid(user)) {
                log.debug("❌ User account invalid");
                return false;
            }

            String jti = claims.getId();
            Optional<RefreshToken> entityOpt = repo.findByJti(jti);
            if (entityOpt.isEmpty()) {
                log.debug("❌ Refresh token JTI not found in DB: {}", jti);
                return false;
            }

            RefreshToken entity = entityOpt.get();
            if (entity.isRevoked()) {
                log.debug("❌ Refresh token is revoked");
                return false;
            }

            if (entity.getUsedAt() != null) {
                log.debug("❌ Refresh token already used at: {}", entity.getUsedAt());
                return false;
            }

            log.debug("✅ Refresh token is valid");
            return true;

        } catch (Exception e) {
            log.error("❌ Refresh token validation failed: {}", e.getMessage(), e);
            return false;
        }
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("tokenType", String.class);
    }

    public boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        boolean expired = expiration.before(new Date());
        if (expired) {
            log.debug("❌ Token is expired at: {}", expiration);
        }
        return expired;
    }

    private boolean isTokenContentValid(String token, User user) {
        String tokenUsername = extractUsername(token);
        Long tokenUserId = extractUserId(token);
        boolean match = Objects.equals(tokenUsername, user.getUsername()) &&
                Objects.equals(tokenUserId, user.getId());

        if (!match) {
            log.debug("❌ Token content mismatch: tokenUsername={}, user.username={}, tokenUserId={}, user.id={}",
                    tokenUsername, user.getUsername(), tokenUserId, user.getId());
        }

        return match;
    }

    private boolean isUserAccountValid(User user) {
        return user.isEnabled() && user.isAccountNonLocked() && user.isAccountNonExpired();
    }

    private boolean isTokenBlacklisted(String token, BlacklistTokenRepository repo) {
        String hash = hashToken(token);
        boolean blacklisted = repo.existsByToken(hash);

        if (blacklisted) {
            log.debug("❌ Token is blacklisted. Hash: {}", hash);
        }

        return blacklisted;
    }

    private boolean isJtiPresent(String token) {
        String jti = extractJti(token);
        boolean present = jti != null && !jti.isEmpty();

        if (!present) {
            log.debug("❌ Token missing JTI.");
        }

        return present;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> claims.get("roles", List.class));
    }

    public List<String> extractPermissions(String token) {
        return extractClaim(token, claims -> claims.get("permissions", List.class));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            log.error("❌ Token expired: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        } catch (MalformedJwtException e) {
            log.error("❌ Malformed JWT token: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INVALID_TOKEN);
        } catch (JwtException e) {
            log.error("❌ JWT validation failed: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new AppException(ErrorCode.HASH_TOKEN_FAILED);
        }
    }

    private Key getSignKey() {
        byte[] keyBytes = hexStringToByteArray(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    public long getJwtExpiration() {
        return jwtExpiration;
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }
}
