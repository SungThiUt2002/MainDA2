package com.stu.account_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionInfo {
    private String jti;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
