package com.stu.account_service.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LogoutRequest {
    private String refreshToken;
}
