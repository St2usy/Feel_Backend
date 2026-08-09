package com.feel.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "calendar_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dateStart;

    @Column(nullable = false)
    private LocalDate dateEnd;

    @Column(nullable = false, length = 200)
    private String eventKorean;

    @Column(length = 200)
    private String eventEnglish;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 비즈니스 로직: 종료일이 시작일 이후인지 검증
    @PrePersist
    @PreUpdate
    protected void validateDates() {
        if (dateEnd != null && dateStart != null && dateEnd.isBefore(dateStart)) {
            throw new IllegalArgumentException("종료일은 시작일 이후여야 합니다.");
        }
    }
}
