package com.feel.backend.controller;

import com.feel.backend.dto.ErrorResponse;
import com.feel.backend.dto.NoticeRequestDto;
import com.feel.backend.dto.NoticeResponseDto;
import com.feel.backend.service.AuthService;
import com.feel.backend.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final AuthService authService;

    // 공지사항 생성 (파일 업로드 지원)
    @PostMapping
    public ResponseEntity<?> createNotice(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @Valid @ModelAttribute NoticeRequestDto requestDto,
        @RequestParam(required = false) MultipartFile image
    ) {
        try {
            authService.validateAuthHeader(authHeader);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        NoticeResponseDto response = noticeService.createNotice(requestDto, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 여러 공지사항 일괄 생성
    @PostMapping("/batch")
    public ResponseEntity<?> createNotices(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @Valid @RequestBody List<NoticeRequestDto> requestDtos
    ) {
        try {
            authService.validateAuthHeader(authHeader);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        List<NoticeResponseDto> responses = noticeService.createNotices(requestDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    // 전체 공지사항 조회 (페이징 + 카테고리 필터)
    @GetMapping
    public ResponseEntity<Page<NoticeResponseDto>> getAllNotices(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String category
    ) {
        Page<NoticeResponseDto> notices = noticeService.getAllNotices(page, size, category);
        return ResponseEntity.ok(notices);
    }

    // 고정 공지사항 조회
    @GetMapping("/pinned")
    public ResponseEntity<List<NoticeResponseDto>> getPinnedNotices() {
        List<NoticeResponseDto> notices = noticeService.getPinnedNotices();
        return ResponseEntity.ok(notices);
    }

    // 특정 공지사항 조회
    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponseDto> getNoticeById(@PathVariable Long id) {
        NoticeResponseDto notice = noticeService.getNoticeById(id);
        return ResponseEntity.ok(notice);
    }

    // 공지사항 수정 (파일 업로드 지원)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNotice(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @PathVariable Long id,
        @Valid @ModelAttribute NoticeRequestDto requestDto,
        @RequestParam(required = false) MultipartFile image
    ) {
        try {
            authService.validateAuthHeader(authHeader);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        NoticeResponseDto response = noticeService.updateNotice(id, requestDto, image);
        return ResponseEntity.ok(response);
    }

    // 공지사항 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotice(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @PathVariable Long id
    ) {
        try {
            authService.validateAuthHeader(authHeader);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }

    // 카테고리별 조회
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<NoticeResponseDto>> getNoticesByCategory(
        @PathVariable String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<NoticeResponseDto> notices = noticeService.getNoticesByCategory(category, page, size);
        return ResponseEntity.ok(notices);
    }

    // 검색 (카테고리 필터 지원)
    @GetMapping("/search")
    public ResponseEntity<Page<NoticeResponseDto>> searchNotices(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String category
    ) {
        Page<NoticeResponseDto> notices = noticeService.searchNotices(keyword, page, size, category);
        return ResponseEntity.ok(notices);
    }
}
