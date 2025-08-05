package com.stu.account_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "jti", length = 36, nullable = false, unique = true)
    private String jti; // lưu id token

    @NotNull
    @Column(name = "token_hash", length = 64, nullable = false, unique = true)
    private String tokenHash; // lưu token dưới dnagj mã hóa

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // lưu id user

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // thời gian hết hạn cảu toekn --> Để biết khi nào cần cleanup token

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // Thời gian tạo token

    @Column(name = "used_at")
    private LocalDateTime usedAt; // (= null --> chưa dùng)(thời gian token được sử dụng (nếu đã dùng)

    @Column(nullable = false)
    private boolean revoked = false; // kiểm tra đã bị đăng xuất chưa

//    private String ipAddress; // địa chỉ
//    private String userAgent; //

//    @NotNull
//    @Enumerated(EnumType.STRING)
//    @Column(name = "status", nullable = false)
//    @Builder.Default
//    private TokenStatus status = TokenStatus.ACTIVE; // trạng thái cur token
//
//    public enum TokenStatus {
//        ACTIVE, // token mới tạo chưa sử dụng
//        USED, // Token đã được sử dụng để fresh --> để đảm bảo chỉ dduocj sử dụng một lần
//        REVOKED, // token bị revoke --> logout ( chưa hết hạn mà đã logout)
//        EXPIRED // token hết hạn tự nhiên
//    }



}
