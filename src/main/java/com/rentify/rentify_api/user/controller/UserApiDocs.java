package com.rentify.rentify_api.user.controller;

import com.rentify.rentify_api.post.dto.PostDetailResponse;
import com.rentify.rentify_api.user.dto.CreateUserRequest;
import com.rentify.rentify_api.user.dto.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "User API", description = "회원 가입, 로그인 등 사용자 관련 API")
public interface UserApiDocs {

    @Operation(summary = "회원가입", description = "<strong>멱등성 키(UUID) 헤더 필수</strong><br/>신규 사용자를 등록합니다.")
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
                    value = "{\"success\": true, \"code\": \"201\", \"message\": \"회원가입 성공\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (유효성 검사 실패)",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "이메일 형식 검증 실패",
                        value = "{\"success\": false, \"code\": \"400\", \"message\": \"email : 이메일 형식이 올바르지 않습니다.\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "계좌번호 형식 검증 실패",
                        value = "{\"success\": false, \"code\": \"400\", \"message\": \"계좌번호는 숫자만 사용한 10 ~ 20자리여야 합니다.\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "휴대폰 번호 형식 검증 실패",
                        value = "{\"success\": false, \"code\": \"400\", \"message\": \"휴대폰 번호는 숫자만 사용한 10~11자리여야 합니다.\", \"data\": null}"
                    ),
                }
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "이미 가입된 회원 또는 회원가입 처리 중",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    summary = "중복된 요청(멱등성 처리)",
                    value = "{\"success\": false, \"code\": \"409\", \"message\": \"이전 요청이 아직 처리 중입니다. 잠시 후 결과를 확인해주세요.\", \"data\": null}"
                )
            )
        )
    })
    @PostMapping
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<Void>> createUser(
        @Parameter(
            name = "Idempotency-Key",
            description = "중복 요청 방지를 위한 멱등성 키",
            required = true,
            in = ParameterIn.HEADER,
            example = "123e4567-e89b-12d3-a456-426614174000"
        )
        @RequestHeader(value = "Idempotency-Key") UUID idempotencyKey,
        @RequestBody(
            description = "회원가입 요청 데이터",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CreateUserRequest.class)
            )
        )
        @Valid CreateUserRequest request
    );

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 쿠키로 발급받습니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "로그인 성공 (JWT 토큰이 쿠키에 설정됨)",
            headers = @Header(
                name = "Set-Cookie",
                description = "accessToken (HttpOnly, 24시간 유효)",
                schema = @Schema(type = "string", example = "accessToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...; Path=/; Max-Age=86400; HttpOnly")
            ),
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"code\": \"200\", \"message\": \"로그인 성공\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "비밀번호 불일치",
                        value = "{\"success\": false, \"code\": \"401\", \"message\": \"비밀번호가 일치하지 않습니다.\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "비활성화된 계정",
                        value = "{\"success\": false, \"code\": \"401\", \"message\": \"비활성화된 계정입니다.\", \"data\": null}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": false, \"code\": \"404\", \"message\": \"존재하지 않는 사용자입니다.\", \"data\": null}"
                )
            )
        )
    })
    @PostMapping("/login")
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<Void>> login(
        @RequestBody(
            description = "로그인 요청 데이터",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginRequest.class),
                examples = @ExampleObject(
                    value = "{\"email\": \"user@example.com\", \"password\": \"1234\"}"
                )
            )
        )
        @org.springframework.web.bind.annotation.RequestBody LoginRequest request, HttpServletResponse httpResponse
    );

    @Operation(summary = "로그아웃", description = "로그아웃하여 RefreshToken 및 쿠키의 토큰 제거합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "로그아웃 성공 (쿠키의 토큰들이 삭제됨)",
            headers = {
                @Header(
                    name = "Set-Cookie",
                    description = "accessToken 삭제 (Max-Age=0)",
                    schema = @Schema(type = "string", example = "accessToken=; Path=/; Max-Age=0; HttpOnly")
                ),
                @Header(
                    name = "Set-Cookie",
                    description = "refreshToken 삭제 (Max-Age=0)",
                    schema = @Schema(type = "string", example = "refreshToken=; Path=/; Max-Age=0; HttpOnly")
                )
            },
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"code\": \"200\", \"message\": \"로그아웃 성공\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패 (토큰이 없거나 유효하지 않음)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": false, \"code\": \"401\", \"message\": \"인증이 필요합니다.\", \"data\": null}"
                )
            )
        )
    })
    @PostMapping("/logout")
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<Void>> logout(
        @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
        HttpServletResponse response
    );

    @Operation(
        summary = "내 게시글 조회",
        description = "로그인한 사용자가 자신이 등록한 모든 게시글을 페이징하여 조회합니다.<br/> " +
            "기본적으로 HIDDEN 상태의 게시글은 제외되며, includeHidden=true로 설정 시 포함됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "success": true,
                          "code": "200",
                          "message": "OK",
                          "data": {
                            "content": [
                              {
                                "postId": 1,
                                "userId": 1,
                                "categoryName": "갤럭시 울트라",
                                "userName": "서성민",
                                "title": "갤럭시 S25엣지 대여",
                                "description": "테스트 수정 입니다",
                                "pricePerDay": 52000,
                                "maxRentalDays": 20,
                                "isParcel": true,
                                "isMeetup": true,
                                "status": "AVAILABLE",
                                "imageUrls": ["http://unirental.duckdns.org/images/6d82b234-6708-4875-ba60-80af76cc9e69.jpg",
                                              "http://unirental.duckdns.org/images/20689993-ef98-4919-b8e9-631448253749.jpg"],
                                "createAt": "2026-01-28T20:55:58.522954",
                                "updateAt": "2026-01-29T20:45:03.049727"
                              }
                            ],
                            "pageable": {
                              "pageNumber": 0,
                              "pageSize": 20,
                              "sort": {
                                "sorted": true,
                                "unsorted": false,
                                "empty": false
                              }
                            },
                            "totalElements": 15,
                            "totalPages": 1,
                            "last": true,
                            "size": 20,
                            "number": 0,
                            "first": true,
                            "numberOfElements": 15,
                            "empty": false
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패 (로그인 필요)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": false, \"code\": \"401\", \"message\": \"인증이 필요합니다.\", \"data\": null}"
                )
            )
        )
    })
    @Parameters({
        @Parameter(
            name = "page",
            description = "페이지 번호 (0부터 시작)",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "integer", defaultValue = "0")
        ),
        @Parameter(
            name = "size",
            description = "한 페이지당 개수",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "integer", defaultValue = "20")
        ),
        @Parameter(
            name = "sort",
            description = "정렬 기준 (필드명,방향). 예: createAt,desc",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "array", implementation = String.class),
            example = "createAt,desc"
        )
    })
    @GetMapping("/me/posts")
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<Page<PostDetailResponse>>> getMyPosts(
        @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
        @Parameter(
            name = "includeHidden",
            description = "HIDDEN 상태의 게시글 포함 여부 (기본값: false)",
            example = "false"
        )
        @RequestParam(defaultValue = "false") boolean includeHidden,
        @ParameterObject Pageable pageable
    );
}
