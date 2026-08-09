package com.feel.backend.dto;

import com.feel.backend.entity.ActivityCategory;
import com.feel.backend.entity.ActivityPost;
import com.feel.backend.entity.RecruitmentStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityPostResponseDto {

    private Long id;
    private ActivityCategory category;
    private String title;
    private String content;
    private String thumbnailUrl;
    private Integer viewCount;
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String organization;
    private LocalDate startDate;
    private LocalDate endDate;
    private String applyUrl;

    private Integer headcount;
    private String recruitmentRoles;
    private String contactUrl;
    private RecruitmentStatus status;

    public static ActivityPostResponseDto fromEntity(ActivityPost entity) {
        return ActivityPostResponseDto.builder()
            .id(entity.getId())
            .category(entity.getCategory())
            .title(entity.getTitle())
            .content(entity.getContent())
            .thumbnailUrl(entity.getThumbnailUrl())
            .viewCount(entity.getViewCount() != null ? entity.getViewCount() : 0)
            .author(entity.getAuthor())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .organization(entity.getOrganization())
            .startDate(entity.getStartDate())
            .endDate(entity.getEndDate())
            .applyUrl(entity.getApplyUrl())
            .headcount(entity.getHeadcount())
            .recruitmentRoles(entity.getRecruitmentRoles())
            .contactUrl(entity.getContactUrl())
            .status(entity.getStatus())
            .build();
    }
}
