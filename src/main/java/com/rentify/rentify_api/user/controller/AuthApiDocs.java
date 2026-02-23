package com.rentify.rentify_api.user.controller;

import com.rentify.rentify_api.user.dto.AuthMeResponse;
import com.rentify.rentify_api.user.dto.SendVerificationCodeRequest;
import com.rentify.rentify_api.user.dto.VerifyEmailRequest;
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
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
                    value = "{\"success\": true, \"code\": \"200\", \"message\": \"요청이 성공적으로 처리되었습니다.\", \"data\": {\"userId\": 1, \"email\": \"user@example.com\", \"name\": \"홍길동\"}}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "인증 실패 - 토큰이 없거나 유효하지 않음",
            content = @Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
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
                    value = "{\"success\": true, \"code\": \"200\", \"message\": \"토큰 갱신 성공\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "RefreshToken이 쿠키에 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": false, \"code\": \"400\", \"message\": \"RefreshToken이 없습니다.\", \"data\": null}"
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
                        value = "{\"success\": false, \"code\": \"404\", \"message\": \"RefreshToken을 찾을 수 없습니다.\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "토큰 만료",
                        value = "{\"success\": false, \"code\": \"404\", \"message\": \"만료되거나 무효화된 RefreshToken입니다.\", \"data\": null}"
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
                    value = "{\"success\": false, \"code\": \"401\", \"message\": \"비활성화된 계정입니다.\", \"data\": null}"
                )
            )
        )
    })
    @PostMapping("/refresh")
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<Void>> refreshToken(HttpServletRequest request, HttpServletResponse response);

    @Operation(
        summary = "이메일 인증 코드 요청",
        description = "사용자의 이메일을 파라미터로 받아 해당 메일 주소로 인증 코드가 담긴 메일을 송신합니다.<br/>"
            + "메일 발신은 비동기로 처리 하기에 수신까지 시간이 소요될 수 있으며 인증 만료 시간은 요청 시간으로부터 5분 뒤 입니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "이메일 발신 성공(비동기)",
            content = @Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "{\"success\": true, \"code\": \"200\", \"message\": \"인증 번호가 전송되었습니다.\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "이미 가입된 회원",
            content = @Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "{\"success\": false, \"code\": \"409\", \"message\": \"이미 가입된 계정입니다.\", \"data\": null}"
                )
            )
        )
    })
    @PostMapping("/email-verification/code")
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<Void>> sendVerificationCode(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "이메일 인증 코드 요청 데이터",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SendVerificationCodeRequest.class)
            )
        )
        @Valid @RequestBody SendVerificationCodeRequest request
    );

    @Operation(
        summary = "이메일 인증 요청",
        description = "이메일 인증 코드 요청을 한 이메일과 이메일로 전송된 코드로 이메일 인증을 진행합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "이메일 인증 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "{\"success\": true, \"code\": \"200\", \"message\": \"이메일 인증이 완료되었습니다.\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "인증 내역 요청 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "{\"success\": false, \"code\": \"404\", \"message\": \"인증 요청 내역이 없습니다.\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "인증 오류",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "인증 시간 만료",
                        value = """
                            {
                                "success": false,
                                "code": "400",
                                "message": "인증 시간이 만료되었습니다.",
                                "data": null
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "인증 번호 불일치",
                        value = """
                            {
                                "success": false,
                                "code": "400",
                                "message": "인증 번호가 일치하지 않습니다.",
                                "data": null
                            }
                            """
                    )
                }
            )
        )
    })
    @PostMapping("/email-verification/code/verify")
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<Void>> verifyEmail(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "이메일 인증 요청 데이터",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = VerifyEmailRequest.class),
                examples = @ExampleObject(
                    value = "{\"email\": \"user@example.com\", \"code\": \"132413\"}"
                )
            )
        )
        @Valid @RequestBody VerifyEmailRequest request
    );
}
