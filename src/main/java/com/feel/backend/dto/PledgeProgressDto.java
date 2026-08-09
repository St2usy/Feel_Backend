package com.feel.backend.dto;

import lombok.*;

import java.util.Map;

/**
 * 공약 이행 progress 조회/수정용 DTO.
 * - GET: id -> completed 매핑 (프론트에서 ID로 매칭)
 * - PATCH: id, completed 단건 또는 Map 형태로 일괄 수정
 */
public class PledgeProgressDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProgressResponse {
        /** 공약 ID -> 이행 여부 */
        private Map<String, Boolean> progress;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchUpdateRequest {
        /** 공약 ID -> 이행 여부. 일괄 수정 시 사용 */
        private Map<String, Boolean> progress;
    }
}
