package com.feel.backend.dto;

import com.feel.backend.entity.ActivityCategory;
import com.feel.backend.entity.RecruitmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityPostRequestDto {

    @NotNull(message = "카테고리는 필수입니다.")
    private ActivityCategory category;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 500)
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @NotBlank(message = "작성자는 필수입니다.")
    @Size(max = 100)
    private String author;

    /** 썸네일은 multipart로 별도 업로드 시 설정됨. URL 직접 입력도 허용 */
    @Size(max = 500)
    private String thumbnailUrl;

    // ---------- 정보성 (대외활동/공모전) ----------
    @Size(max = 200)
    private String organization;
    private LocalDate startDate;
    private LocalDate endDate;
    @Size(max = 500)
    private String applyUrl;

    // ---------- 팀원 모집 ----------
    /** 모집 인원 (명) */
    private Integer headcount;
    @Size(max = 500)
    private String recruitmentRoles;
    @Size(max = 500)
    private String contactUrl;
    private RecruitmentStatus status;
}
