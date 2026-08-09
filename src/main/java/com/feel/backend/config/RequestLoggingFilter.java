package com.feel.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * /api/resources 요청이 백엔드에 도달하는지 확인하기 위한 로그 필터.
 * 업로드 시 Docker 로그에 아무것도 안 보이면 요청이 이 컨테이너로 오지 않는 것.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path != null && path.startsWith("/api/resources")) {
            log.info("[RequestLoggingFilter] {} {} → 컨테이너 도달", request.getMethod(), path);
        }
        filterChain.doFilter(request, response);
    }
}
