package com.rentify.rentify_api.user.controller;

import com.rentify.rentify_api.user.dto.AuthMeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth", description = "인증 API")
public interface AuthApi {

    @Operation(summary = "내 정보 조회", description = "JWT로 현재 로그인 사용자 확인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 성공"),
            @ApiResponse(responseCode = "401", description = "Access Token 만료",
                    content = @Content(schema = @Schema(example = """
            {
              "code": "TOKEN_EXPIRED",
              "message": "Access token expired"
            }
            """))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰",
                    content = @Content(schema = @Schema(example = """
            {
              "code": "INVALID_TOKEN",
              "message": "Invalid token"
            }
            """)))
    })
    ResponseEntity<AuthMeResponse> me();
}
