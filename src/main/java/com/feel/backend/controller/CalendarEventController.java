package com.feel.backend.controller;

import com.feel.backend.dto.CalendarEventRequestDto;
import com.feel.backend.dto.CalendarEventResponseDto;
import com.feel.backend.dto.ErrorResponse;
import com.feel.backend.service.AuthService;
import com.feel.backend.service.CalendarEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendar/events")
@RequiredArgsConstructor
public class CalendarEventController {

    private final CalendarEventService calendarEventService;
    private final AuthService authService;

    // 전체 행사 일정 조회 (캐시 가능)
    @GetMapping("/all")
    public ResponseEntity<List<CalendarEventResponseDto>> getAllEvents() {
        List<CalendarEventResponseDto> events = calendarEventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    // 조건부 행사 일정 조회
    @GetMapping
    public ResponseEntity<List<CalendarEventResponseDto>> getEvents(
        @RequestParam(required = false) String year,
        @RequestParam(required = false) String month,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<CalendarEventResponseDto> events = calendarEventService.getEvents(year, month, startDate, endDate);
        return ResponseEntity.ok(events);
    }

    // 특정 행사 일정 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Long id) {
        try {
            CalendarEventResponseDto event = calendarEventService.getEventById(id);
            return ResponseEntity.ok(event);
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                .message(e.getMessage())
                .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    // 행사 일정 생성
    @PostMapping
    public ResponseEntity<?> createEvent(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CalendarEventRequestDto requestDto
    ) {
        try {
            authService.validateAuthHeader(authHeader);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        try {
            CalendarEventResponseDto event = calendarEventService.createEvent(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(event);
        } catch (IllegalArgumentException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                .message(e.getMessage())
                .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                .message("행사 일정 생성에 실패했습니다: " + e.getMessage())
                .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // 행사 일정 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody CalendarEventRequestDto requestDto
    ) {
        try {
            authService.validateAuthHeader(authHeader);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        try {
            CalendarEventResponseDto event = calendarEventService.updateEvent(id, requestDto);
            return ResponseEntity.ok(event);
        } catch (IllegalArgumentException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                .message(e.getMessage())
                .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                .message(e.getMessage())
                .build();
            HttpStatus status = e.getMessage().contains("찾을 수 없습니다")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    // 행사 일정 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        try {
            authService.validateAuthHeader(authHeader);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        try {
            calendarEventService.deleteEvent(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                .message(e.getMessage())
                .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}
