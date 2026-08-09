package com.feel.backend.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ResourceFileDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private String category;
        private String title;
        private String description;
        private Integer year;
        private Integer month;
        private LocalDate eventDate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String category;
        private String originalFileName;
        private String fileUrl;
        private String fileType;
        private Long fileSize;
        private String title;
        private String description;
        private Integer year;
        private Integer month;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvailablePeriods {
        private List<Integer> years;
        private List<MonthData> months;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class MonthData {
            private Integer year;
            private List<Integer> months;
        }
    }

    /** 리소스 메타 수정용 (제목, 설명, 행사일) */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String title;
        private String description;
        private LocalDate eventDate;
    }
}
