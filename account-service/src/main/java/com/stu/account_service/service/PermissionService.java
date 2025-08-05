package com.stu.account_service.service;

import com.stu.account_service.entity.Permission;
import com.stu.account_service.repository.PermissionRepository;
import com.stu.account_service.dto.request.CreatePermissionRequest;
import com.stu.account_service.dto.response.PermissionResponse;
import com.stu.account_service.exception.AppException;
import com.stu.account_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionService {
    private final PermissionRepository permissionRepository;

    // create permission by admin
    @PreAuthorize("hasRole('ADMIN')")
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        // kiểm tra xem permission đã tồn tại chưa
        if (permissionRepository.findByName(request.getName()).isPresent()) {
            throw new AppException(ErrorCode.PERMISSION_EXISTED);
        }

        Permission permission = Permission.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        // lưu vao database
        Permission saved = permissionRepository.save(permission);
        return toResponse(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PermissionResponse updatePermission(Long id, CreatePermissionRequest request) {
        // keiemr tra xem permission này đã có (đã tồn tại chưa)
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        // tiến hành update
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        //lưu
        Permission saved = permissionRepository.save(permission);
        // kết quả trả về
        return toResponse(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deletePermission(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new AppException(ErrorCode.PERMISSION_NOT_FOUND);
        }
        permissionRepository.deleteById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PermissionResponse getPermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        return toResponse(permission);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    private PermissionResponse toResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .build();
    }
} 