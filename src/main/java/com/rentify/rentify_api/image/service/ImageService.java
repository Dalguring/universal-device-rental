package com.rentify.rentify_api.image.service;

import com.rentify.rentify_api.image.exception.FileLimitExceededException;
import com.rentify.rentify_api.image.exception.FileSizeExceededException;
import com.rentify.rentify_api.image.exception.FileTypeNotAllowedException;
import com.rentify.rentify_api.image.repository.ImageRepository;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.base.url:http://localhost:8080}")
    private String baseUrl;

    @Transactional
    public List<String> uploadImages(List<MultipartFile> files) {
        validateFiles(files);

        File uploadDir = new File(uploadPath).getAbsoluteFile();
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (!created) {
                throw new RuntimeException("업로드 디렉토리 생성 실패: " + uploadDir.getAbsolutePath());
            }
            log.info("Upload directory created: {}", uploadPath);
        }

        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String savedFileName = UUID.randomUUID() + extension;

                File dest = new File(uploadDir, savedFileName);
                file.transferTo(dest);

                String imageUrl = baseUrl + "/images/" + savedFileName;
                imageUrls.add(imageUrl);

                log.info("파일 저장 완료: {} -> {}, URL: {}", originalFilename, savedFileName, imageUrl);

            } catch (IOException e) {
                log.error("파일 저장 실패: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("파일 저장에 실패했습니다.", e);
            }
        }

        return imageUrls;
    }

    // TODO: post 저장 시 image table 저장

    private void validateFiles(List<MultipartFile> files) {
        if (files.size() > 5) {
            throw new FileLimitExceededException();
        }

        files.forEach(file -> {
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new FileSizeExceededException();
            }

            String contentType = file.getContentType();
            if (contentType == null ||
                (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                throw new FileTypeNotAllowedException();
            }
        });
    }

}
