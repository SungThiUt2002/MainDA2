package com.stu.account_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stu.account_service.dto.response.ApiResponse;
import com.stu.account_service.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
        // Đăng ký module JavaTimeModule để hỗ trợ Java 8 time types
        this.objectMapper.registerModule(new JavaTimeModule());
        // Tắt việc serialize dates thành timestamps
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.error("Unauthorized error: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .data(request.getRequestURI())
                .message(ErrorCode.UNAUTHENTICATED.getMessage())
                .code(ErrorCode.UNAUTHENTICATED.getCode())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}

/*
Khi người dùng không có token hoặc token không hợp lệ, JwtAuthenticationEntryPoint sẽ:

Không redirect về login page (hành vi mặc định của Spring Security cho web).

Thay vào đó, trả về JSON lỗi với mã HTTP 401 Unauthorized cho client REST.

Khắc phục lỗi Jackson serialization với LocalDateTime bằng cách:
- Đăng ký JavaTimeModule
- Tắt WRITE_DATES_AS_TIMESTAMPS
*/