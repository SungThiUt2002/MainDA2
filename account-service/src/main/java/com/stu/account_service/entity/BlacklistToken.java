package com.stu.account_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "blacklist_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000, nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expiry_date", nullable = true)
    private LocalDateTime expiryDate; // thời gian hết hạn --> để dọn dẹp

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // thời gian đăng xuất

    // Lifecycle callback để tự động set createdAt
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

//Lưu những token khi chưa hết hạn mà người dùng đăng xuất