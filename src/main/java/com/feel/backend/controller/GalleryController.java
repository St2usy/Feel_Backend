package com.feel.backend.controller;

import com.feel.backend.dto.ErrorResponse;
import com.feel.backend.dto.GalleryRequestDto;
import com.feel.backend.dto.GalleryResponseDto;
import com.feel.backend.service.AuthService;
import com.feel.backend.service.GalleryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/gallery")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "https://m-se0k.github.io"})
public class GalleryController {

    private final GalleryService galleryService;
    private final AuthService authService;

    // 갤러리 생성 (파일 업로드 필수)
    @PostMapping
    public ResponseEntity<?> createGallery(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @ModelAttribute GalleryRequestDto requestDto,
            @RequestParam MultipartFile image
    ) {
        try {
            authService.validateAuthHeader(authHeader);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        GalleryResponseDto response = galleryService.createGallery(requestDto, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 전체 갤러리 조회 (페이징 + 카테고리 필터)
    @GetMapping
    public ResponseEntity<Page<GalleryResponseDto>> getAllGalleries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String category
    ) {
        Page<GalleryResponseDto> galleries = galleryService.getAllGalleries(page, size, category);
        return ResponseEntity.ok(galleries);
    }

    // 최근 갤러리 조회 (최대 10개)
    @GetMapping("/recent")
    public ResponseEntity<List<GalleryResponseDto>> getRecentGalleries() {
        List<GalleryResponseDto> galleries = galleryService.getRecentGalleries();
        return ResponseEntity.ok(galleries);
    }

    // 특정 갤러리 조회
    @GetMapping("/{id}")
    public ResponseEntity<GalleryResponseDto> getGalleryById(@PathVariable Long id) {
        GalleryResponseDto gallery = galleryService.getGalleryById(id);
        return ResponseEntity.ok(gallery);
    }

    // 갤러리 수정 (파일 업로드 선택)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGallery(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @ModelAttribute GalleryRequestDto requestDto,
            @RequestParam(required = false) MultipartFile image
    ) {
        try {
            authService.validateAuthHeader(authHeader);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        GalleryResponseDto response = galleryService.updateGallery(id, requestDto, image);
        return ResponseEntity.ok(response);
    }

    // 갤러리 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGallery(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        try {
            authService.validateAuthHeader(authHeader);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        galleryService.deleteGallery(id);
        return ResponseEntity.noContent().build();
    }

    // 검색 (카테고리 필터 지원)
    @GetMapping("/search")
    public ResponseEntity<Page<GalleryResponseDto>> searchGalleries(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String category
    ) {
        Page<GalleryResponseDto> galleries = galleryService.searchGalleries(keyword, page, size, category);
        return ResponseEntity.ok(galleries);
    }
}
