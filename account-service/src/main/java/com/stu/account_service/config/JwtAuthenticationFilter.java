package com.stu.account_service.config;

import com.stu.account_service.entity.User;
import com.stu.account_service.exception.AppException;
import com.stu.account_service.repository.BlacklistTokenRepository;
import com.stu.account_service.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final BlacklistTokenRepository blacklistRepo;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            String tokenType = jwtUtil.extractTokenType(token);
            if (!"access".equals(tokenType)) {
                log.debug("Token type is not access, reject authentication");
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtUtil.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userService.loadUserByUsername(username);
                if (jwtUtil.isTokenValid(token, user, blacklistRepo)) {
                    setAuthenticationContext(user, request);
                    log.info("Authenticated user: {}, IP: {}, Roles: {}, Permissions: {}",
                            user.getUsername(),
                            request.getRemoteAddr(),
                            jwtUtil.extractRoles(token),
                            jwtUtil.extractPermissions(token));
                } else {
                    log.debug("Token validation failed for user: {}", username);
                }
            }
        } catch (AppException e) {
            log.debug("JWT validation failed: {} - Error code: {}", e.getMessage(), e.getErrorCode().getCode());
        } catch (Exception e) {
            log.error("Unexpected JWT processing error: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthenticationContext(User user, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}