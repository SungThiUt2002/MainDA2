package com.stu.account_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
        @NotBlank(message = "USERNAME_INVALID")
        private String usernameOrEmail;

        @NotBlank(message = "INVALID_PASSWORD")
        @Size(min = 8, message = "INVALID_PASSWORD")
        private String password;
}
