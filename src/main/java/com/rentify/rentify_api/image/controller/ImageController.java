package com.rentify.rentify_api.image.controller;

import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.image.service.ImageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<List<String>>> upload(
        @RequestParam("files") List<MultipartFile> files
    ) {
        List<String> imageUrls = imageService.uploadImages(files);
        return ResponseEntity.ok(ApiResponse.success(imageUrls));
    }

    @GetMapping("{filename}")
    public ResponseEntity<ApiResponse<Void>> getImage(@PathVariable String filename) {
        return ResponseEntity.ok(ApiResponse.success());
    }
}
