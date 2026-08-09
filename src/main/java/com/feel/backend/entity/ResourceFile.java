package com.feel.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resource_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String category; // inspection, finance, gallery, study-support

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String storedFileName;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private String fileType; // image/jpeg, application/pdf 등

    @Column(nullable = false)
    private Long fileSize;

    @Column(length = 200)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "report_year")
    private Integer year;

    @Column(name = "report_month")
    private Integer month;

    /** 등록/행사일. admin에서 행사일 입력 시 그 값, 미입력 시 업로드 시각 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
