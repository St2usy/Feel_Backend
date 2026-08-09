package com.feel.backend.service;

import com.feel.backend.dto.NoticeRequestDto;
import com.feel.backend.dto.NoticeResponseDto;
import com.feel.backend.entity.Notice;
import com.feel.backend.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final FileStorageService fileStorageService;

    // 공지사항 생성 (파일 업로드 지원)
    @Transactional
    public NoticeResponseDto createNotice(NoticeRequestDto requestDto, MultipartFile imageFile) {
        // 이미지 파일 저장
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = fileStorageService.storeFile(imageFile);
            imageUrl = "/uploads/" + fileName;
        }

        Notice notice = Notice.builder()
            .title(requestDto.getTitle())
            .content(requestDto.getContent())
            .author(requestDto.getAuthor())
            .isPinned(requestDto.getIsPinned())
            .category(requestDto.getCategory())
            .imageUrl(imageUrl)
            .viewCount(0)
            .build();

        Notice savedNotice = noticeRepository.save(notice);
        return NoticeResponseDto.fromEntity(savedNotice);
    }

    // 여러 공지사항 일괄 생성
    @Transactional
    public List<NoticeResponseDto> createNotices(List<NoticeRequestDto> requestDtos) {
        List<Notice> notices = requestDtos.stream()
            .map(dto -> Notice.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .author(dto.getAuthor())
                .isPinned(dto.getIsPinned())
                .category(dto.getCategory())
                .viewCount(0)
                .build())
            .collect(Collectors.toList());

        List<Notice> savedNotices = noticeRepository.saveAll(notices);
        return savedNotices.stream()
            .map(NoticeResponseDto::fromEntity)
            .collect(Collectors.toList());
    }

    // 전체 공지사항 조회 (페이징 + 카테고리 필터)
    public Page<NoticeResponseDto> getAllNotices(int page, int size, String category) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 카테고리가 null이거나 빈 문자열이면 전체 조회
        if (category == null || category.trim().isEmpty()) {
            return noticeRepository.findAll(pageable)
                .map(NoticeResponseDto::fromEntity);
        }

        // 카테고리가 지정되면 해당 카테고리만 조회
        return noticeRepository.findByCategoryOrderByCreatedAtDesc(category, pageable)
            .map(NoticeResponseDto::fromEntity);
    }

    // 고정 공지사항 조회
    public List<NoticeResponseDto> getPinnedNotices() {
        return noticeRepository.findByIsPinnedTrueOrderByCreatedAtDesc()
            .stream()
            .map(NoticeResponseDto::fromEntity)
            .collect(Collectors.toList());
    }

    // 특정 공지사항 조회 (조회수 증가)
    @Transactional
    public NoticeResponseDto getNoticeById(Long id) {
        Notice notice = noticeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("요청한 공지사항을 찾을 수 없습니다."));

        notice.incrementViewCount();
        return NoticeResponseDto.fromEntity(notice);
    }

    // 공지사항 수정 (파일 업로드 지원)
    @Transactional
    public NoticeResponseDto updateNotice(Long id, NoticeRequestDto requestDto, MultipartFile imageFile) {
        Notice notice = noticeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("요청한 공지사항을 찾을 수 없습니다."));

        // 기본 필드 업데이트
        notice.setTitle(requestDto.getTitle());
        notice.setContent(requestDto.getContent());
        notice.setAuthor(requestDto.getAuthor());
        notice.setIsPinned(requestDto.getIsPinned());
        notice.setCategory(requestDto.getCategory());

        // 이미지 파일이 새로 업로드된 경우
        if (imageFile != null && !imageFile.isEmpty()) {
            // 기존 이미지 파일 삭제
            if (notice.getImageUrl() != null) {
                String oldFileName = fileStorageService.extractFileName(notice.getImageUrl());
                fileStorageService.deleteFile(oldFileName);
            }

            // 새 이미지 파일 저장
            String fileName = fileStorageService.storeFile(imageFile);
            notice.setImageUrl("/uploads/" + fileName);
        }

        return NoticeResponseDto.fromEntity(notice);
    }

    // 공지사항 삭제
    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("요청한 공지사항을 찾을 수 없습니다."));

        // 이미지 파일 삭제
        if (notice.getImageUrl() != null) {
            String fileName = fileStorageService.extractFileName(notice.getImageUrl());
            fileStorageService.deleteFile(fileName);
        }

        noticeRepository.deleteById(id);
    }

    // 카테고리별 조회
    public Page<NoticeResponseDto> getNoticesByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return noticeRepository.findByCategoryOrderByCreatedAtDesc(category, pageable)
            .map(NoticeResponseDto::fromEntity);
    }

    // 검색 (카테고리 필터 지원)
    public Page<NoticeResponseDto> searchNotices(String keyword, int page, int size, String category) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 카테고리가 null이거나 빈 문자열이면 전체 검색
        if (category == null || category.trim().isEmpty()) {
            return noticeRepository.findByTitleContainingOrContentContainingOrderByCreatedAtDesc(
                keyword, keyword, pageable)
                .map(NoticeResponseDto::fromEntity);
        }

        // 카테고리가 지정되면 해당 카테고리 내에서만 검색
        return noticeRepository.findByCategoryAndTitleContainingOrCategoryAndContentContainingOrderByCreatedAtDesc(
            category, keyword, category, keyword, pageable)
            .map(NoticeResponseDto::fromEntity);
    }
}
