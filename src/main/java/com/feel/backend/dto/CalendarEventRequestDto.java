package com.feel.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class CalendarEventRequestDto {

    @NotNull(message = "시작 날짜는 필수입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("date_start")
    private LocalDate dateStart;

    @NotNull(message = "종료 날짜는 필수입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("date_end")
    private LocalDate dateEnd;

    @NotBlank(message = "행사명(한글)은 필수입니다.")
    @Size(min = 1, max = 200, message = "행사명(한글)은 1자 이상 200자 이하여야 합니다.")
    @JsonProperty("event_korean")
    private String eventKorean;

    @Size(max = 200, message = "행사명(영문)은 200자 이하여야 합니다.")
    @JsonProperty("event_english")
    private String eventEnglish;

    private String description;
}
