package com.feel.backend.config;

import com.feel.backend.entity.AdminUser;
import com.feel.backend.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:}")
    private String adminUsername;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (!StringUtils.hasText(adminUsername) || !StringUtils.hasText(adminPassword)) {
            log.info("Admin seed skipped: app.admin.username/password is not set");
            return;
        }

        if (adminUserRepository.findByUsername(adminUsername).isPresent()) {
            log.info("Admin account already exists: {}", adminUsername);
            return;
        }

        adminUserRepository.save(AdminUser.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .build());

        log.info("Admin account created: {}", adminUsername);
    }
}
