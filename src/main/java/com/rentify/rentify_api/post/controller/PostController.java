package com.rentify.rentify_api.post.controller;

import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.post.dto.PostDetailResponse;
import com.rentify.rentify_api.post.dto.PostFormRequest;
import com.rentify.rentify_api.post.dto.PostFormResponse;
import com.rentify.rentify_api.post.entity.PostStatus;
import com.rentify.rentify_api.post.service.PostService;
import jakarta.validation.Valid;
import java.net.URI;
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

    private final PostService postService;

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
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(@PathVariable Long id) {
        PostDetailResponse response = postService.getPost(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Override
    public ResponseEntity<ApiResponse<PostFormResponse>> createPost(
        @RequestHeader(value = "Idempotency-Key") UUID idempotencyKey,
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody PostFormRequest request
    ) {
        Long postId = postService.createPost(idempotencyKey, userId, request);
        URI location = URI.create("/api/posts/" + postId);
        return ResponseEntity.created(location)
            .body(ApiResponse.success("게시글이 생성되었습니다.", new PostFormResponse(postId)));
    }

    @PutMapping("{id}")
    @Override
    public ResponseEntity<ApiResponse<PostFormResponse>> updatePost(
        @PathVariable Long id,
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody PostFormRequest request
    ) {
        Long postId = postService.updatePost(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("게시글 수정 성공", new PostFormResponse(postId)));
    }

}
