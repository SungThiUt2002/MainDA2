package com.stu.account_service.config;

import com.stu.account_service.entity.Permission;
import com.stu.account_service.entity.Role;
import com.stu.account_service.entity.User;
import com.stu.account_service.repository.PermissionRepository;
import com.stu.account_service.repository.RoleRepository;
import com.stu.account_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    public CommandLineRunner initRolesAndPermissions(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // ===== 1. Tạo các permission =====
            Permission readUser = createPermissionIfNotExists(permissionRepository,
                    "READ_USER", "Read user information");
            Permission updateUser = createPermissionIfNotExists(permissionRepository,
                    "UPDATE_USER", "Update user information");
            Permission deleteUser = createPermissionIfNotExists(permissionRepository,
                    "DELETE_USER", "Delete user");
            Permission createUser = createPermissionIfNotExists(permissionRepository,
                    "CREATE_USER", "Create new user");
            Permission readAllUsers = createPermissionIfNotExists(permissionRepository,
                    "READ_ALL_USERS", "Read all users");
            Permission manageRoles = createPermissionIfNotExists(permissionRepository,
                    "MANAGE_ROLES", "Manage user roles");
            Permission managePermissions = createPermissionIfNotExists(permissionRepository,
                    "MANAGE_PERMISSIONS", "Manage permissions");

            // ===== 2. Tạo hoặc cập nhật role USER =====
            Role userRole = roleRepository.findByName("USER")
                    .orElseGet(() -> Role.builder()
                            .name("USER")
                            .description("Regular user role")
                            .build());
            userRole.setPermissions(Set.of(readUser, updateUser));
            roleRepository.save(userRole);

            // ===== 3. Tạo hoặc cập nhật role ADMIN =====
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> Role.builder()
                            .name("ADMIN")
                            .description("Administrator role")
                            .build());
            adminRole.setPermissions(Set.of(readUser, updateUser, deleteUser, createUser,
                    readAllUsers, manageRoles, managePermissions));
            roleRepository.save(adminRole);

            // Lấy lại role từ database để đảm bảo là "managed entity"
            Role managedAdminRole = roleRepository.findByName("ADMIN").orElseThrow();

            // ===== 4. Tạo user admin nếu chưa tồn tại =====
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("Admin123!@#"))
                        .email("admin2002@gmail.com")
                        .roles(Set.of(managedAdminRole))
                        .enabled(true)
                        .build();
                userRepository.save(admin);
                System.out.println("✅ Admin user created with username: admin and password: Admin123!@#");
            }

            System.out.println("✅ Roles, permissions, and default admin initialized successfully!");
        };
    }

    private Permission createPermissionIfNotExists(PermissionRepository repository,
                                                   String name, String description) {
        return repository.findByName(name)
                .orElseGet(() -> repository.save(
                        Permission.builder()
                                .name(name)
                                .description(description)
                                .build()));
    }
}
