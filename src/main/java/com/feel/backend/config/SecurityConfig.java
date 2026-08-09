package com.feel.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * PasswordEncoder만 사용 (spring-security-crypto).
 * spring-boot-starter-security 미사용 → HTTP 요청 차단 없음.
 *
 * 나중에 Spring Security 필터를 추가할 경우, Preflight(CORS)를 위해
 * OPTIONS 요청은 반드시 통과시킬 것:
 * .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
