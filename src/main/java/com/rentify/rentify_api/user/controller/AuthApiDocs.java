package com.rentify.rentify_api.user.controller;

import com.rentify.rentify_api.user.dto.AuthMeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

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

    @Operation(
        summary = "토큰 갱신",
        description = "쿠키의 RefreshToken을 검증하여 새로운 AccessToken을 발급합니다. AccessToken 만료 시 호출하여 로그인을 유지할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "토큰 갱신 성공 (새로운 AccessToken이 쿠키에 설정됨)",
            headers = @Header(
                name = "Set-Cookie",
                description = "새로운 accessToken (HttpOnly, 24시간 유효)",
                schema = @Schema(type = "string", example = "accessToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...; Path=/; Max-Age=86400; HttpOnly")
            ),
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"code\": \"SUCCESS\", \"message\": \"토큰 갱신 성공\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "RefreshToken이 쿠키에 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": false, \"code\": \"INVALID_REQUEST\", \"message\": \"RefreshToken이 없습니다.\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "RefreshToken이 DB에 없거나 만료됨",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "토큰 없음",
                        value = "{\"success\": false, \"code\": \"NOT_FOUND\", \"message\": \"RefreshToken을 찾을 수 없습니다.\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "토큰 만료",
                        value = "{\"success\": false, \"code\": \"NOT_FOUND\", \"message\": \"만료되거나 무효화된 RefreshToken입니다.\", \"data\": null}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "비활성화된 계정",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": false, \"code\": \"ACCOUNT_DEACTIVATED\", \"message\": \"비활성화된 계정입니다.\", \"data\": null}"
                )
            )
        )
    })
    @PostMapping("/refresh")
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<Void>> refreshToken(HttpServletRequest request, HttpServletResponse response);
}
