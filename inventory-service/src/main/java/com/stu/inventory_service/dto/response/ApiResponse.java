package com.stu.inventory_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Chuẩn hóa response trả về cho client từ Product Service.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class ApiResponse<T> {
    private int code;
    private String message;
    private T result;
    private LocalDateTime timestamp;
    private String path;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("OK")
                .result(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
