package com.rentify.rentify_api.post.controller;

import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.post.dto.CreatePostRequest;
import com.rentify.rentify_api.post.entity.PostStatus;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

public interface PostApiDocs {

    ResponseEntity<ApiResponse<Void>> getPosts(
        @RequestParam Long categoryId,
        @RequestParam PostStatus status,
        @RequestParam String keyword,
        @PageableDefault Pageable pageable
    );

    ResponseEntity<ApiResponse<Void>> getPost(
        @PathVariable Long postId
    );

    @PostMapping
    ResponseEntity<ApiResponse<Void>> createPost(
        @RequestHeader(value = "Idempotency-Key") UUID idempotencyKey,
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody CreatePostRequest request
    );

    ResponseEntity<ApiResponse<Void>> updatePost(
        @PathVariable Long postId
    );
}
