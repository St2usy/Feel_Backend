package com.feel.backend.service;

import com.feel.backend.dto.ActivityPostRequestDto;
import com.feel.backend.dto.ActivityPostResponseDto;
import com.feel.backend.entity.ActivityCategory;
import com.feel.backend.entity.ActivityPost;
import com.feel.backend.repository.ActivityPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityPostService {

    private static final String UPLOAD_SUBDIR = "activities";

    private final ActivityPostRepository activityPostRepository;
    private final FileStorageService fileStorageService;

    /**
     * 카테고리별 목록 조회 (페이징, 정렬)
     * @param sort latest | deadline | viewCount
     */
    public Page<ActivityPostResponseDto> getList(ActivityCategory category, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityPost> result;
        if ("deadline".equalsIgnoreCase(sort)) {
            result = activityPostRepository.findByCategoryOrderByEndDateAsc(category, pageable);
        } else if ("viewCount".equalsIgnoreCase(sort) || "views".equalsIgnoreCase(sort)) {
            result = activityPostRepository.findByCategoryOrderByViewCountDesc(category, pageable);
        } else {
            result = activityPostRepository.findByCategoryOrderByCreatedAtDesc(category, pageable);
        }
        return result.map(ActivityPostResponseDto::fromEntity);
    }

    @Transactional
    public ActivityPostResponseDto getById(Long id) {
        ActivityPost post = activityPostRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("요청한 게시글을 찾을 수 없습니다."));
        post.incrementViewCount();
        return ActivityPostResponseDto.fromEntity(post);
    }

    @Transactional
    public ActivityPostResponseDto create(ActivityPostRequestDto dto, MultipartFile thumbnailFile) {
        String thumbnailUrl = null;
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            String relativePath = fileStorageService.storeFileInSubdir(thumbnailFile, UPLOAD_SUBDIR);
            thumbnailUrl = "/uploads/" + relativePath;
        } else if (dto.getThumbnailUrl() != null && !dto.getThumbnailUrl().isBlank()) {
            thumbnailUrl = dto.getThumbnailUrl();
        }

        ActivityPost post = ActivityPost.builder()
            .category(dto.getCategory())
            .title(dto.getTitle())
            .content(dto.getContent())
            .thumbnailUrl(thumbnailUrl)
            .viewCount(0)
            .author(dto.getAuthor())
            .organization(dto.getOrganization())
            .startDate(dto.getStartDate())
            .endDate(dto.getEndDate())
            .applyUrl(dto.getApplyUrl())
            .headcount(dto.getHeadcount())
            .recruitmentRoles(dto.getRecruitmentRoles())
            .contactUrl(dto.getContactUrl())
            .status(dto.getStatus())
            .build();
        ActivityPost saved = activityPostRepository.save(post);
        return ActivityPostResponseDto.fromEntity(saved);
    }

    @Transactional
    public ActivityPostResponseDto update(Long id, ActivityPostRequestDto dto, MultipartFile thumbnailFile) {
        ActivityPost post = activityPostRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("요청한 게시글을 찾을 수 없습니다."));

        post.setCategory(dto.getCategory());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setAuthor(dto.getAuthor());
        post.setOrganization(dto.getOrganization());
        post.setStartDate(dto.getStartDate());
        post.setEndDate(dto.getEndDate());
        post.setApplyUrl(dto.getApplyUrl());
        post.setHeadcount(dto.getHeadcount());
        post.setRecruitmentRoles(dto.getRecruitmentRoles());
        post.setContactUrl(dto.getContactUrl());
        post.setStatus(dto.getStatus());

        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            if (post.getThumbnailUrl() != null) {
                String oldPath = fileStorageService.extractFileName(post.getThumbnailUrl());
                fileStorageService.deleteFile(oldPath);
            }
            String relativePath = fileStorageService.storeFileInSubdir(thumbnailFile, UPLOAD_SUBDIR);
            post.setThumbnailUrl("/uploads/" + relativePath);
        } else if (dto.getThumbnailUrl() != null) {
            post.setThumbnailUrl(dto.getThumbnailUrl());
        }

        return ActivityPostResponseDto.fromEntity(post);
    }

    @Transactional
    public void delete(Long id) {
        ActivityPost post = activityPostRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("요청한 게시글을 찾을 수 없습니다."));
        if (post.getThumbnailUrl() != null) {
            String path = fileStorageService.extractFileName(post.getThumbnailUrl());
            fileStorageService.deleteFile(path);
        }
        activityPostRepository.deleteById(id);
    }
}
