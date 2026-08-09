package com.feel.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(
                "http://engsc.jbnu.ac.kr",
                "https://engsc.jbnu.ac.kr",
                "http://engsc-admin.jbnu.ac.kr",
                "https://engsc-admin.jbnu.ac.kr",
                "http://local-engsc.jbnu.ac.kr",
                "https://local-engsc.jbnu.ac.kr",
                "http://feel-test.com",
                "https://feel-test.com",
                "http://admin.feel-test.com",
                "https://admin.feel-test.com",
                "http://localhost",
                "http://localhost:3000",
                "https://localhost:3000",
                "http://localhost:3001",
                "http://localhost:5173",
                "http://admin.localhost",
                "http://feel-test.113.198.66.98.nip.io",
                "https://feel-test.113.198.66.98.nip.io",
                "http://admin.feel-test.113.198.66.98.nip.io",
                "https://admin.feel-test.113.198.66.98.nip.io",
                "http://api.feel-test.113.198.66.98.nip.io"
            )
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("Content-Type", "Authorization")
            .allowCredentials(true)
            .maxAge(3600);

        // 업로드된 이미지 파일 접근을 위한 CORS 설정
        registry.addMapping("/uploads/**")
            .allowedOrigins(
                "http://engsc.jbnu.ac.kr",
                "https://engsc.jbnu.ac.kr",
                "http://engsc-admin.jbnu.ac.kr",
                "https://engsc-admin.jbnu.ac.kr",
                "http://local-engsc.jbnu.ac.kr",
                "https://local-engsc.jbnu.ac.kr",
                "http://feel-test.com",
                "https://feel-test.com",
                "http://admin.feel-test.com",
                "https://admin.feel-test.com",
                "http://localhost",
                "http://localhost:3000",
                "https://localhost:3000",
                "http://localhost:3001",
                "http://localhost:5173",
                "http://admin.localhost",
                "http://feel-test.113.198.66.98.nip.io",
                "https://feel-test.113.198.66.98.nip.io",
                "http://admin.feel-test.113.198.66.98.nip.io",
                "https://admin.feel-test.113.198.66.98.nip.io",
                "http://api.feel-test.113.198.66.98.nip.io"
            )
            .allowedMethods("GET", "OPTIONS")
            .allowedHeaders("Content-Type", "Authorization")
            .allowCredentials(true)
            .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드된 파일을 정적 리소스로 제공
        // 절대 경로로 설정하여 어디서든 접근 가능하도록 함
        String absolutePath = new java.io.File(uploadDir).getAbsolutePath();
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:///" + absolutePath.replace("\\", "/") + "/");
    }
}
