package com.feel.backend.service;

import com.feel.backend.dto.ResourceFileDto;
import com.feel.backend.entity.ResourceFile;
import com.feel.backend.repository.ResourceFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResourceFileService {

    private final ResourceFileRepository resourceFileRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> ALLOWED_PDF_TYPES = Arrays.asList(
            "application/pdf"
    );

    private static final List<String> VALID_CATEGORIES = Arrays.asList(
            "inspection", "finance", "gallery", "study-support"
    );

    @Transactional
    public ResourceFileDto.Response uploadFile(
            String category,
            String title,
            String description,
            Integer year,
            Integer month,
            LocalDate eventDate,
            MultipartFile file
    ) {
        // 카테고리 유효성 검사
        if (!VALID_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + category);
        }

        // 파일 타입 검사
        String contentType = file.getContentType();
        if (category.equals("finance")) {
            if (!ALLOWED_PDF_TYPES.contains(contentType)) {
                throw new IllegalArgumentException("회계내역은 PDF 파일만 업로드 가능합니다.");
            }
        } else {
            if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
                throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다. (jpeg, png, gif, webp)");
            }
        }

        // 파일 저장
        String originalFileName = file.getOriginalFilename();
        String extension = getExtension(originalFileName);
        String storedFileName = UUID.randomUUID().toString() + "." + extension;
        
        // 카테고리별 디렉토리 생성
        Path categoryPath = Paths.get(uploadDir, "resources", category);
        try {
            Files.createDirectories(categoryPath);
            Path filePath = categoryPath.resolve(storedFileName);
            Files.write(filePath, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }

        String fileUrl = baseUrl + "/uploads/resources/" + category + "/" + storedFileName;

        // DB 저장 (행사일 입력 시 created_at에 반영, 없으면 업로드 시각)
        LocalDateTime createdAt = eventDate != null ? eventDate.atStartOfDay() : LocalDateTime.now();
        ResourceFile resourceFile = ResourceFile.builder()
                .category(category)
                .originalFileName(originalFileName)
                .storedFileName(storedFileName)
                .fileUrl(fileUrl)
                .fileType(contentType)
                .fileSize(file.getSize())
                .title(title)
                .description(description)
                .year(year)
                .month(month)
                .createdAt(createdAt)
                .build();

        ResourceFile saved = resourceFileRepository.save(resourceFile);
        return toResponse(saved);
    }

    // 이전 버전 호환 (year, month, eventDate 없이 호출 시)
    @Transactional
    public ResourceFileDto.Response uploadFile(
            String category,
            String title,
            String description,
            Integer year,
            Integer month,
            MultipartFile file
    ) {
        return uploadFile(category, title, description, year, month, null, file);
    }

    @Transactional
    public ResourceFileDto.Response uploadFile(
            String category,
            String title,
            String description,
            MultipartFile file
    ) {
        return uploadFile(category, title, description, null, null, null, file);
    }

    @Transactional
    public ResourceFileDto.Response updateFileMeta(Long id, ResourceFileDto.UpdateRequest request) {
        ResourceFile file = resourceFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("요청한 파일을 찾을 수 없습니다."));
        if (request.getTitle() != null) {
            file.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            file.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            file.setCreatedAt(request.getEventDate().atStartOfDay());
        }
        return toResponse(resourceFileRepository.save(file));
    }

    public Page<ResourceFileDto.Response> getFilesByCategory(String category, int page, int size) {
        if (!VALID_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + category);
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ResourceFile> files = resourceFileRepository.findByCategoryOrderByCreatedAtDesc(category, pageable);
        return files.map(this::toResponse);
    }

    public List<ResourceFileDto.Response> getAllFilesByCategory(String category) {
        if (!VALID_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + category);
        }
        
        List<ResourceFile> files = resourceFileRepository.findByCategoryOrderByCreatedAtDesc(category);
        return files.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ResourceFileDto.Response getFileById(Long id) {
        ResourceFile file = resourceFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("요청한 파일을 찾을 수 없습니다."));
        return toResponse(file);
    }

    @Transactional
    public void deleteFile(Long id) {
        ResourceFile file = resourceFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("요청한 파일을 찾을 수 없습니다."));

        // 실제 파일 삭제
        Path filePath = Paths.get(uploadDir, "resources", file.getCategory(), file.getStoredFileName());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // 파일 삭제 실패해도 DB에서는 삭제
        }

        resourceFileRepository.delete(file);
    }

    public long getCountByCategory(String category) {
        return resourceFileRepository.countByCategory(category);
    }

    // 연도/월별 조회
    public List<ResourceFileDto.Response> getFilesByCategoryAndYearMonth(
            String category, Integer year, Integer month) {
        if (!VALID_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + category);
        }

        List<ResourceFile> files = resourceFileRepository
                .findByCategoryAndYearAndMonthOrderByCreatedAtDesc(category, year, month);
        return files.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // 연도별 조회
    public List<ResourceFileDto.Response> getFilesByCategoryAndYear(String category, Integer year) {
        if (!VALID_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + category);
        }

        List<ResourceFile> files = resourceFileRepository
                .findByCategoryAndYearOrderByMonthDescCreatedAtDesc(category, year);
        return files.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // 카테고리별 사용 가능한 연도 목록
    public List<Integer> getAvailableYears(String category) {
        return resourceFileRepository.findDistinctYearsByCategory(category);
    }

    // 카테고리/연도별 사용 가능한 월 목록
    public List<Integer> getAvailableMonths(String category, Integer year) {
        return resourceFileRepository.findDistinctMonthsByCategoryAndYear(category, year);
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private ResourceFileDto.Response toResponse(ResourceFile file) {
        return ResourceFileDto.Response.builder()
                .id(file.getId())
                .category(file.getCategory())
                .originalFileName(file.getOriginalFileName())
                .fileUrl(file.getFileUrl())
                .fileType(file.getFileType())
                .fileSize(file.getFileSize())
                .title(file.getTitle())
                .description(file.getDescription())
                .year(file.getYear())
                .month(file.getMonth())
                .createdAt(file.getCreatedAt())
                .build();
    }
}
