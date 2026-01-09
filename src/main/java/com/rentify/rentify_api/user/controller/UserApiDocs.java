package com.rentify.rentify_api.user.controller;

import com.rentify.rentify_api.user.dto.CreateUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@Tag(name = "USER API", description = "회원 가입, 로그인 등 사용자 관련 API")
public interface UserApiDocs {

    @Operation(summary = "회원가입", description = "신규 사용자를 등록합니다. <strong>멱등성 키(UUID) 헤더 필수</strong>")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    headers = @Header(
                            name = HttpHeaders.LOCATION,
                            description = "생성된 리소스 URI",
                            schema = @Schema(type = "string", example = "/api/users/152")
                    ),
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Success",
                                    value = "{\"success\": true, \"code\": \"SUCCESS\", \"message\": \"회원가입 성공\", \"data\": null}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효성 검사 실패)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "BadRequest",
                                    value = "{\"success\": false, \"code\": \"INVALID_REQUEST\", \"message\": \"email : 이메일 형식이 올바르지 않습니다.\", \"data\": null}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 가입된 회원 또는 회원가입 처리 중",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Conflict",
                                    summary = "멱등성 처리 중 예시",
                                    value = "{\"success\": false, \"code\": \"PROCESS_IN_PROGRESS\", \"message\": \"이전 요청이 아직 처리 중입니다. 잠시 후 결과를 확인해주세요.\", \"data\": null}"
                            )
                    )
            )
    })
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<Void>> createUser(
            @Parameter(
                    name = "Idempotency-Key",
                    description = "중복 요청 방지를 위한 멱등성 키",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            UUID idempotencyKey,

            @RequestBody(
                    description = "회원가입 요청 데이터",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateUserRequest.class)
                    )
            )
            CreateUserRequest request
    );
}
