package com.feel.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("파일 저장 디렉토리 생성 완료: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("파일 저장 디렉토리를 생성할 수 없습니다.", ex);
        }
    }

    /**
     * 파일을 저장하고 저장된 파일명을 반환합니다.
     */
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 원본 파일명 정리
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // 파일명에 부적절한 문자가 있는지 확인
            if (originalFileName.contains("..")) {
                throw new RuntimeException("파일명에 부적절한 경로가 포함되어 있습니다: " + originalFileName);
            }

            // 파일 확장자 추출
            String fileExtension = "";
            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                fileExtension = originalFileName.substring(lastDotIndex);
            }

            // UUID를 사용하여 고유한 파일명 생성
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // 파일 저장 경로 생성
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);

            // 파일 저장
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("파일 저장 완료: {} -> {}", originalFileName, newFileName);

            return newFileName;
        } catch (IOException ex) {
            throw new RuntimeException("파일 저장에 실패했습니다: " + originalFileName, ex);
        }
    }

    /**
     * 지정한 하위 디렉토리에 파일을 저장하고, 접근 URL 경로를 반환합니다.
     * 예: subDir="activities" -> "activities/uuid.jpg" 반환, DB에는 "/uploads/activities/uuid.jpg" 저장
     */
    public String storeFileInSubdir(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (originalFileName.contains("..")) {
                throw new RuntimeException("파일명에 부적절한 경로가 포함되어 있습니다: " + originalFileName);
            }
            String fileExtension = "";
            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                fileExtension = originalFileName.substring(lastDotIndex);
            }
            String newFileName = UUID.randomUUID().toString() + fileExtension;
            Path subDirPath = this.fileStorageLocation.resolve(subDir);
            Files.createDirectories(subDirPath);
            Path targetLocation = subDirPath.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("파일 저장 완료: {} -> {}/{}", originalFileName, subDir, newFileName);
            return subDir + "/" + newFileName;
        } catch (IOException ex) {
            throw new RuntimeException("파일 저장에 실패했습니다: " + originalFileName, ex);
        }
    }

    /**
     * 파일을 삭제합니다. (하위 디렉 포함 경로 지원, 예: "activities/uuid.jpg")
     */
    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }

        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            log.info("파일 삭제 완료: {}", fileName);
        } catch (IOException ex) {
            log.error("파일 삭제 실패: {}", fileName, ex);
        }
    }

    /**
     * 이미지 URL에서 저장 경로 상대 경로를 추출합니다.
     * 예: "/uploads/activities/uuid.jpg" -> "activities/uuid.jpg"
     *     "/uploads/abc.jpg" -> "abc.jpg"
     */
    public String extractFileName(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        String path = imageUrl;
        if (path.startsWith("/uploads/")) {
            path = path.substring("/uploads/".length());
        }
        return path.isEmpty() ? null : path;
    }
}
