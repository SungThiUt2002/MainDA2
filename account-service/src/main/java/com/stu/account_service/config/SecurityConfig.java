package com.stu.account_service.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ✅ CHỈNH Ở ĐÂY
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/auth/register", "/auth/refresh", "/auth/logout").permitAll()
                        .requestMatchers("/users/delete/{userId}").permitAll()
                        .requestMatchers("/api/address/{id}/users/{userId}").permitAll()
                        .requestMatchers("/api/address/all/{userId}").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000")); // ✅ nên chỉ định rõ origin
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

/*
LUỒNG XỬ LÝ: Gửi http request đến spring security sẽ xử lý như sau:
KHi SecurityFilterChain được kích hoạt nó sẽ khởi tạo chuỗi filter mặc định
    csrf().disable() – tắt bảo vệ CSRF (vì dùng JWT, không dùng cookie)

    sessionManagement().sessionCreationPolicy(STATELESS) – không dùng session

    authorizeHttpRequests() – xác định quyền truy cập các endpoint

    addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class) – thêm filter tùy chỉnh kiểm tra JWT

    exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint) – gán entry point xử lý lỗi xác thự

 */

// VÍ DỤ CỤ THỂ

/// //////
/*
1. Ví dụ login ( chỉ gửi username + password)
    --> SecurityFilterChain sẽ thực hiện lần lượt các bộ lọc
    + csrf().disable() -->ở đây không kích hoạt
    + kiển tra session
    + kiểm tra quyền truy cập các enpoint (Nếu không có quyền truy cập endpoint thì không thể thực hiện bước tiếp theo)
    + Sau đó mới gọi AuthenticationManager + AuthenticationProvider
    + Kết quả: Trả về một token
 */

/* ví dụ khi logout mà cần gửi kèm token để xác thực
    +     + csrf().disable() -->ở đây không kích hoạt
    + kiển tra session
    + kiểm tra quyền truy cập các enpoint (Nếu không có quyền truy cập endpoint thì không thể thực hiện bước tiếp theo)
    + Gọi đến JwtAuthenticationFilter để:
        . Giải mã, trích xuất thông tin từ token
        . Kiểm tra xem token có hợp lệ không
        . Nếu hợp lệ gán cho SecurityContextHolder -->  thực hiện các nhiệm vụ theo yêu cầu ( như logout)
        . Nếu không hợp lệ  --> Goị đến JwtAuthenticationEntryPoint --> trả về lỗi 401
 */


