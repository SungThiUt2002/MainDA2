package com.stu.account_service.controller;

import com.stu.account_service.dto.request.CreatePermissionRequest;
import com.stu.account_service.dto.response.ApiResponse;
import com.stu.account_service.dto.response.PermissionResponse;
import com.stu.account_service.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    @PostMapping("/create")
    public ApiResponse<PermissionResponse > createPermission(@RequestBody CreatePermissionRequest request) {
        return ApiResponse.<PermissionResponse>builder()
                .data(permissionService.createPermission(request))
                .message("Tạo permission mới thành công")
                .build();
    }

    @PutMapping("/update/{id}")
    public PermissionResponse updatePermission(@PathVariable Long id, @RequestBody CreatePermissionRequest request) {
        return permissionService.updatePermission(id, request);
    }

    @DeleteMapping("/delete/{id}")
    public void deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
    }

    @GetMapping("/{id}")
    public PermissionResponse getPermission(@PathVariable Long id) {
        return permissionService.getPermission(id);
    }

    @GetMapping
    public List<PermissionResponse> getAllPermissions() {
        return permissionService.getAllPermissions();
    }
} 