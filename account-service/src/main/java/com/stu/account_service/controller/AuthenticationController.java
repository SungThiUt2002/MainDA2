package com.stu.account_service.controller;

import com.stu.account_service.dto.request.*;
import com.stu.account_service.dto.response.ApiResponse;
import com.stu.account_service.dto.response.AuthenticationResponse;
import com.stu.account_service.dto.response.LoginAttemptInfo;
import com.stu.account_service.dto.response.UserResponse;
import com.stu.account_service.entity.RefreshToken;
import com.stu.account_service.entity.User;
import com.stu.account_service.service.AuthenticationService;
import com.stu.account_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
//@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private   final UserService userService;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody @Valid LoginRequest request,
                                                     HttpServletRequest httpRequest) {
        log.info("Login request received for: {}", request.getUsernameOrEmail());
        var result = authenticationService.login(request, httpRequest);
        return ApiResponse.<AuthenticationResponse>builder()
                .data(result)
                .message("Đăng nhập thành công")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Logout
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody @Valid LogoutRequest logoutRequest) {
        
        // Lấy access token từ Authorization header
        String accessToken = authorizationHeader;
        if (authorizationHeader.startsWith("Bearer ")) {
            accessToken = authorizationHeader.substring(7);
        }
        
        authenticationService.logout(accessToken, logoutRequest.getRefreshToken());
        return ApiResponse.<Void>builder()
                .message("Đăng xuất thành công")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Dăng ký người dùng
    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody @Valid RegisterRequest request){
        log.info("Tạo user mới: {}", request.getUsername());
        return ApiResponse.<UserResponse>builder()
                .data(userService.createUser(request))
                .message("Đăng ký thành công")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // refresh
    @PostMapping("/refresh")
    private ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshTokenRequest request){
        log.info("Tạo refresh token mới: {}");
        return ApiResponse.<AuthenticationResponse>builder()
                .data(authenticationService.refreshToken(request))
                .message("RefreshToken mới thành công")
                .build();
    }


}
