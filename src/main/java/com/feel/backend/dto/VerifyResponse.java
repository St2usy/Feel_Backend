package com.feel.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyResponse {
    private boolean valid;
    private String username; // 이메일
    private String nickname; // users 테이블 닉네임 (있으면)
}
