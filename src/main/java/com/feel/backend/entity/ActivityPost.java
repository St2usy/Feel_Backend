package com.feel.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 대외활동 / 공모전 / 팀원 모집 게시물
 * - 대외활동·공모전: Admin 업로드, 사용자 조회만
 * - 팀원 모집: 로그인 사용자 CRUD
 */
@Entity
@Table(name = "activity_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityCategory category;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(nullable = false, length = 100)
    private String author;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ---------- 정보성 필드 (대외활동/공모전) ----------
    @Column(length = 200)
    private String organization;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "apply_url", length = 500)
    private String applyUrl;

    // ---------- 매칭 필드 (팀원 모집) ----------
    /** 모집 인원 (명) */
    @Column(name = "headcount")
    private Integer headcount;

    /** 프론트/백엔드/기획 등 태그 (JSON 배열 또는 쉼표 구분 문자열) */
    @Column(name = "recruitment_roles", length = 500)
    private String recruitmentRoles;

    @Column(name = "contact_url", length = 500)
    private String contactUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RecruitmentStatus status;

    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }
}
