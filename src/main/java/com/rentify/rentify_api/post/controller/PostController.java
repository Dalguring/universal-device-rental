package com.rentify.rentify_api.post.controller;

import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.post.dto.CreatePostRequest;
import com.rentify.rentify_api.post.entity.PostStatus;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController implements PostApiDocs {

    @Override
    @GetMapping()
    public ResponseEntity<ApiResponse<Void>> getPosts(
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) PostStatus status,
        @RequestParam(required = false) String keyword,
        @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Override
    @GetMapping("{id}")
    public ResponseEntity<ApiResponse<Void>> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping
    @Override
    public ResponseEntity<ApiResponse<Void>> createPost(
        @RequestHeader(value = "Idempotency-Key") UUID idempotencyKey,
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody CreatePostRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Override
    @PutMapping("{id}")
    public ResponseEntity<ApiResponse<Void>> updatePost(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success());
    }

}
