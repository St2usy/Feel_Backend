package com.feel.backend.dto;

import com.feel.backend.entity.CalendarEvent;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarEventResponseDto {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("date_start")
    private LocalDate dateStart;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("date_end")
    private LocalDate dateEnd;

    @JsonProperty("event_korean")
    private String eventKorean;

    @JsonProperty("event_english")
    private String eventEnglish;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public static CalendarEventResponseDto fromEntity(CalendarEvent event) {
        return CalendarEventResponseDto.builder()
            .id(event.getId())
            .dateStart(event.getDateStart())
            .dateEnd(event.getDateEnd())
            .eventKorean(event.getEventKorean())
            .eventEnglish(event.getEventEnglish())
            .description(event.getDescription())
            .createdAt(event.getCreatedAt())
            .updatedAt(event.getUpdatedAt())
            .build();
    }
}
