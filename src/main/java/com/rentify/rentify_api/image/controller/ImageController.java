package com.rentify.rentify_api.image.controller;

import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.image.dto.ImageUploadResponse;
import com.rentify.rentify_api.image.service.ImageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController implements ImageApiDocs {

    private final ImageService imageService;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageUploadResponse>> upload(
        @RequestPart("files") List<MultipartFile> files
    ) {
        List<String> imageUrls = imageService.uploadImages(files);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(
                HttpStatus.CREATED, "이미지 등록완료", new ImageUploadResponse(imageUrls))
            );
    }
}
