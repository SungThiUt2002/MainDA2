package com.stu.account_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public  class LoginAttemptInfo {
    private int failedAttempts;
    private int maxAttempts;
    private boolean isLocked;
    private LocalDateTime lockedUntil;
    private int remainingAttempts;
}
