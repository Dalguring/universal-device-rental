package com.rentify.rentify_api.post.controller;

import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.post.entity.CategoryStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public interface PostApiDocs {

    ResponseEntity<ApiResponse<Void>> getPosts(
            @RequestParam Long categoryId,
            @RequestParam CategoryStatus status,
            @RequestParam String keyword,
            @PageableDefault Pageable pageable
    );

    ResponseEntity<ApiResponse<Void>> getPost(
            @PathVariable Long postId
    );

    ResponseEntity<ApiResponse<Void>> createPost(

    );

    ResponseEntity<ApiResponse<Void>> updatePost(
            @PathVariable Long postId
    );
}
