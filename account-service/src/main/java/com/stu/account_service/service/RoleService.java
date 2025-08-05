package com.stu.account_service.service;

import com.stu.account_service.entity.Role;
import com.stu.account_service.entity.Permission;
import com.stu.account_service.repository.RoleRepository;
import com.stu.account_service.repository.PermissionRepository;
import com.stu.account_service.dto.request.CreateRoleRequest;
import com.stu.account_service.dto.response.RoleResponse;
import com.stu.account_service.dto.response.PermissionResponse;
import com.stu.account_service.exception.AppException;
import com.stu.account_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    //hasAuthority hoặc hasPermission dùng cho permission,

    // chỉ có admin
    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse createRole(CreateRoleRequest request) {
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }
        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        if (request.getPermissionName() != null) {
            Set<Permission> permissions = request.getPermissionName().stream()
                    .map(pname -> permissionRepository.findByName(pname)
                            .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND)))
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }
        Role saved = roleRepository.save(role);
        return toResponse(saved);
    }

    // update role theo name role
//    @PreAuthorize("hasRole('ADMIN')")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_USER')")
    public RoleResponse updateRole(Long id, CreateRoleRequest request) {
       // tìm role theo id
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        //
        //role.setName(request.getName());
        role.setDescription(request.getDescription());

        // kếu cột permission khác rỗng thì thêm permisssion, nếu = rỗng, bỏ qua
        if (request.getPermissionName() != null) {
            Set<Permission> permissions = request.getPermissionName().stream()
                    .map(pname -> permissionRepository.findByName(pname)
                            .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND)))
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }
        Role saved = roleRepository.save(role);
        return toResponse(saved);
    }

    // có thể xóa theo id hoặc theo tên role vì tên role cũng là unique
//    @PreAuthorize("hasRole('ADMIN')")
//    public void deleteRole(Long id) {
//        if (!roleRepository.existsById(id)) {
//            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
//        }
//        roleRepository.deleteById(id);
//    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }
        roleRepository.deleteById(id);
    }


    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse getRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        return toResponse(role);
    }

    // Lấy toàn bộ dnah sách role
    @PreAuthorize("hasRole('ADMIN')")
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public RoleResponse toResponse(Role role) {
        Set<PermissionResponse> permissionResponses = role.getPermissions().stream()
            .map(p -> PermissionResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .build())
            .collect(Collectors.toSet());
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(permissionResponses)
                .build();
    }
} 