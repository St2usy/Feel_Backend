package com.feel.backend.service;

import com.feel.backend.dto.GalleryRequestDto;
import com.feel.backend.dto.GalleryResponseDto;
import com.feel.backend.entity.Gallery;
import com.feel.backend.repository.GalleryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GalleryService {

    private final GalleryRepository galleryRepository;
    private final FileStorageService fileStorageService;

    // 갤러리 생성 (파일 업로드 필수)
    @Transactional
    public GalleryResponseDto createGallery(GalleryRequestDto requestDto, MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new RuntimeException("이미지 파일은 필수입니다.");
        }

        // 이미지 파일 저장
        String fileName = fileStorageService.storeFile(imageFile);
        String imageUrl = "/uploads/" + fileName;

        Gallery gallery = Gallery.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .photographer(requestDto.getPhotographer())
                .category(requestDto.getCategory())
                .imageUrl(imageUrl)
                .viewCount(0)
                .build();

        Gallery savedGallery = galleryRepository.save(gallery);
        return GalleryResponseDto.fromEntity(savedGallery);
    }

    // 전체 갤러리 조회 (페이징 + 카테고리 필터)
    public Page<GalleryResponseDto> getAllGalleries(int page, int size, String category) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (category == null || category.trim().isEmpty()) {
            return galleryRepository.findAll(pageable)
                    .map(GalleryResponseDto::fromEntity);
        }

        return galleryRepository.findByCategory(category, pageable)
                .map(GalleryResponseDto::fromEntity);
    }

    // 최근 갤러리 조회 (최대 10개)
    public List<GalleryResponseDto> getRecentGalleries() {
        return galleryRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(GalleryResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 특정 갤러리 조회 (조회수 증가)
    @Transactional
    public GalleryResponseDto getGalleryById(Long id) {
        Gallery gallery = galleryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("요청한 갤러리를 찾을 수 없습니다."));

        gallery.incrementViewCount();
        return GalleryResponseDto.fromEntity(gallery);
    }

    // 갤러리 수정
    @Transactional
    public GalleryResponseDto updateGallery(Long id, GalleryRequestDto requestDto, MultipartFile imageFile) {
        Gallery gallery = galleryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("요청한 갤러리를 찾을 수 없습니다."));

        // 기본 필드 업데이트
        gallery.setTitle(requestDto.getTitle());
        gallery.setDescription(requestDto.getDescription());
        gallery.setPhotographer(requestDto.getPhotographer());
        gallery.setCategory(requestDto.getCategory());

        // 이미지 파일이 새로 업로드된 경우
        if (imageFile != null && !imageFile.isEmpty()) {
            // 기존 이미지 파일 삭제
            if (gallery.getImageUrl() != null) {
                String oldFileName = fileStorageService.extractFileName(gallery.getImageUrl());
                fileStorageService.deleteFile(oldFileName);
            }

            // 새 이미지 파일 저장
            String fileName = fileStorageService.storeFile(imageFile);
            gallery.setImageUrl("/uploads/" + fileName);
        }

        return GalleryResponseDto.fromEntity(gallery);
    }

    // 갤러리 삭제
    @Transactional
    public void deleteGallery(Long id) {
        Gallery gallery = galleryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("요청한 갤러리를 찾을 수 없습니다."));

        // 이미지 파일 삭제
        if (gallery.getImageUrl() != null) {
            String fileName = fileStorageService.extractFileName(gallery.getImageUrl());
            fileStorageService.deleteFile(fileName);
        }

        galleryRepository.deleteById(id);
    }

    // 검색 (카테고리 필터 지원)
    public Page<GalleryResponseDto> searchGalleries(String keyword, int page, int size, String category) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (category == null || category.trim().isEmpty()) {
            return galleryRepository.findByTitleContaining(keyword, pageable)
                    .map(GalleryResponseDto::fromEntity);
        }

        return galleryRepository.findByCategoryAndTitleContaining(category, keyword, pageable)
                .map(GalleryResponseDto::fromEntity);
    }
}
