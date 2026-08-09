package com.feel.backend.service;

import com.feel.backend.dto.CalendarEventRequestDto;
import com.feel.backend.dto.CalendarEventResponseDto;
import com.feel.backend.entity.CalendarEvent;
import com.feel.backend.repository.CalendarEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;

    // 전체 행사 일정 조회 (캐싱 고려 가능 - 필요시 @Cacheable 추가)
    public List<CalendarEventResponseDto> getAllEvents() {
        return calendarEventRepository.findAllByOrderByDateStartAsc()
            .stream()
            .map(CalendarEventResponseDto::fromEntity)
            .collect(Collectors.toList());
    }

    // 조건부 행사 일정 조회
    public List<CalendarEventResponseDto> getEvents(String year, String month, LocalDate startDate, LocalDate endDate) {
        List<CalendarEvent> events;

        if (startDate != null && endDate != null) {
            // 날짜 범위로 조회
            validateDateRange(startDate, endDate);
            events = calendarEventRepository.findByDateRange(startDate, endDate);
        } else if (year != null && month != null) {
            // 연도와 월로 조회
            int yearInt = Integer.parseInt(year);
            int monthInt = Integer.parseInt(month);
            LocalDate startOfMonth = LocalDate.of(yearInt, monthInt, 1);
            LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
            events = calendarEventRepository.findByYearAndMonth(yearInt, monthInt, startOfMonth, endOfMonth);
        } else if (year != null) {
            // 연도로만 조회
            int yearInt = Integer.parseInt(year);
            events = calendarEventRepository.findByYear(yearInt);
        } else {
            // 조건이 없으면 전체 조회
            events = calendarEventRepository.findAllByOrderByDateStartAsc();
        }

        return events.stream()
            .map(CalendarEventResponseDto::fromEntity)
            .collect(Collectors.toList());
    }

    // 특정 행사 일정 상세 조회
    public CalendarEventResponseDto getEventById(Long id) {
        CalendarEvent event = calendarEventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("요청한 행사 일정을 찾을 수 없습니다."));
        return CalendarEventResponseDto.fromEntity(event);
    }

    // 행사 일정 생성
    @Transactional
    public CalendarEventResponseDto createEvent(CalendarEventRequestDto requestDto) {
        // 날짜 유효성 검증
        validateDateRange(requestDto.getDateStart(), requestDto.getDateEnd());

        CalendarEvent event = CalendarEvent.builder()
            .dateStart(requestDto.getDateStart())
            .dateEnd(requestDto.getDateEnd())
            .eventKorean(requestDto.getEventKorean())
            .eventEnglish(requestDto.getEventEnglish())
            .description(requestDto.getDescription())
            .build();

        CalendarEvent savedEvent = calendarEventRepository.save(event);
        return CalendarEventResponseDto.fromEntity(savedEvent);
    }

    // 행사 일정 수정
    @Transactional
    public CalendarEventResponseDto updateEvent(Long id, CalendarEventRequestDto requestDto) {
        CalendarEvent event = calendarEventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("요청한 행사 일정을 찾을 수 없습니다."));

        // 날짜 유효성 검증
        if (requestDto.getDateStart() != null && requestDto.getDateEnd() != null) {
            validateDateRange(requestDto.getDateStart(), requestDto.getDateEnd());
        }

        // 필드 업데이트 (null이 아닌 경우만)
        if (requestDto.getDateStart() != null) {
            event.setDateStart(requestDto.getDateStart());
        }
        if (requestDto.getDateEnd() != null) {
            event.setDateEnd(requestDto.getDateEnd());
        }
        if (requestDto.getEventKorean() != null && !requestDto.getEventKorean().trim().isEmpty()) {
            event.setEventKorean(requestDto.getEventKorean());
        }
        if (requestDto.getEventEnglish() != null) {
            event.setEventEnglish(requestDto.getEventEnglish());
        }
        if (requestDto.getDescription() != null) {
            event.setDescription(requestDto.getDescription());
        }

        return CalendarEventResponseDto.fromEntity(event);
    }

    // 행사 일정 삭제
    @Transactional
    public void deleteEvent(Long id) {
        CalendarEvent event = calendarEventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("요청한 행사 일정을 찾을 수 없습니다."));
        calendarEventRepository.deleteById(id);
    }

    // 날짜 범위 유효성 검증
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일 이후여야 합니다.");
        }
    }
}
