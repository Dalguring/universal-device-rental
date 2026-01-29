package com.rentify.rentify_api.user.controller;

import com.rentify.rentify_api.user.dto.AuthMeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Auth", description = "인증 API")
public interface AuthApiDocs {

    @Operation(
        summary = "내 정보 조회",
        description = "쿠키에 저장된 JWT accessToken으로 현재 로그인한 사용자 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "인증 성공 - 사용자 정보 반환 (userId, email, name)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.rentify.rentify_api.common.response.ApiResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "{\"success\": true, \"code\": \"SUCCESS\", \"message\": \"요청이 성공적으로 처리되었습니다.\", \"data\": {\"userId\": 1, \"email\": \"user@example.com\", \"name\": \"홍길동\"}}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "인증 실패 - 토큰이 없거나 유효하지 않음",
            content = @Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = ""
                )
            )
        )
    })
    @GetMapping("/me")
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<AuthMeResponse>> me(@AuthenticationPrincipal Long userId);
}
