package com.feel.backend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Google ID 토큰 검증
 * https://oauth2.googleapis.com/tokeninfo?id_token=XXX 사용
 */
@Component
@Slf4j
public class GoogleTokenVerifier {

    private static final String TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=%s";

    @Value("${app.google.client-id:}")
    private String googleClientId;

    private final RestTemplate restTemplate = new RestTemplate();

    public GoogleTokenInfo verify(String idToken) {
        String url = String.format(TOKENINFO_URL, idToken);
        try {
            ResponseEntity<GoogleTokenInfo> response = restTemplate.getForEntity(url, GoogleTokenInfo.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GoogleTokenInfo info = response.getBody();
                if (googleClientId != null && !googleClientId.isBlank() && !googleClientId.equals(info.getAud())) {
                    throw new RuntimeException("토큰의 클라이언트 ID가 일치하지 않습니다.");
                }
                return info;
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException re && re.getMessage() != null) {
                throw re;
            }
            log.warn("Google ID 토큰 검증 실패: {}", e.getMessage());
            throw new RuntimeException("유효하지 않은 Google 로그인 정보입니다.");
        }
        throw new RuntimeException("유효하지 않은 Google 로그인 정보입니다.");
    }

    public static final String ALLOWED_DOMAIN = "@jbnu.ac.kr";

    public static boolean isAllowedEmail(String email) {
        return email != null && email.toLowerCase().endsWith(ALLOWED_DOMAIN);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GoogleTokenInfo {
        @JsonProperty("email")
        private String email;

        @JsonProperty("email_verified")
        private String emailVerified;

        @JsonProperty("aud")
        private String aud;

        @JsonProperty("sub")
        private String sub;

        @JsonProperty("name")
        private String name;
    }
}
