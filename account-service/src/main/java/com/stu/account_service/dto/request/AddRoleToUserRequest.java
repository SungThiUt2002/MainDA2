package com.stu.account_service.dto.request;
import com.stu.account_service.Validator.ValidPassword;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddRoleToUserRequest {
    private Long userId;
    private String roleName;
}

// dùng khi admin muốn cấp thêm quyền cho người dùng