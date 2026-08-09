package com.feel.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String username;
    private String nickname;

    /** 신규 사용자: 닉네임 입력 필요 */
    private Boolean needSignup;
    private String email; // needSignup 시 프론트에 전달
}
