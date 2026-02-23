package com.rentify.rentify_api.user.controller;

import com.rentify.rentify_api.post.dto.PostDetailResponse;
import com.rentify.rentify_api.rental.dto.RentalResponse;
import com.rentify.rentify_api.user.dto.CreateUserRequest;
import com.rentify.rentify_api.user.dto.LoginRequest;
import com.rentify.rentify_api.user.dto.PasswordUpdateRequest;
import com.rentify.rentify_api.user.dto.UserUpdateRequest;
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
import org.springframework.web.bind.annotation.PatchMapping;
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
            description = "존재하지 않는 회원",
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
                           "message": "요청이 성공적으로 처리되었습니다.",
                           "data": {
                             "content": [
                               {
                                 "categoryName": "아이폰",
                                 "createAt": "2026-02-01T15:11:24.081793",
                                 "description": "아이폰 17pro 대여합니다.",
                                 "imageUrls": [
                                   "http://unirental.duckdns.org/images/ca7470ce-7133-485d-b0f7-33e5f71724c9.jpg",
                                   "http://unirental.duckdns.org/images/c4ef577c-daaa-4da7-8f7a-963bfc605439.png"
                                 ],
                                 "isMeetup": false,
                                 "isParcel": true,
                                 "maxRentalDays": 30,
                                 "postId": 2,
                                 "pricePerDay": 10000,
                                 "status": "AVAILABLE",
                                 "title": "아이폰 대여",
                                 "updateAt": "2026-02-04T13:05:27.997911",
                                 "userId": 1,
                                 "userName": "서성민"
                               }
                             ],
                             "page": {
                               "size": 20,
                               "number": 0,
                               "totalElements": 2,
                               "totalPages": 1
                             }
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

    @Operation(
            summary = "내 대여 내역 조회",
            description = "로그인한 사용자의 대여 내역을 페이징하여 조회합니다.<br/> " +
                    "role 파라미터 : 빌려준 내역(LENDER), 빌린 내역(BORROWER), 전체(미입력)"
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
                            "rentalId": 15,
                             "userId": 2,
                             "borrowerName": "김빌림",
                             "postId": 1,
                             "title": "갤럭시 S25엣지 대여",
                             "lenderName": "이대여",
                             "startDate": "2026-02-25",
                             "endDate": "2026-02-28",
                             "receiveMethod": "PARCEL",
                             "status": "IN_PROGRESS",
                             "totalPrice": 156000,
                            "createdAt": "2026-02-20T14:30:00.123456",
                            "updatedAt": "2026-02-21T09:15:00.654321"
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
                        "totalElements": 5,
                        "totalPages": 1,
                        "last": true,
                        "size": 20,
                        "number": 0,
                        "first": true,
                        "numberOfElements": 5,
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
                    description = "정렬 기준 (필드명,방향). 예: createdAt,desc",
                    in = ParameterIn.QUERY,
                    schema = @Schema(type = "array", implementation = String.class),
                    example = "createdAt,desc"
            )
    })
    @GetMapping("/me/rentals")
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<Page<RentalResponse>>> getMyRentals(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(
                    name = "role",
                    description = "조회 역할 필터 (예: LENDER(빌려준 내역), BORROWER(빌린 내역))",
                    example = "BORROWER"
            )
            @RequestParam(required = false) String role,
            @ParameterObject Pageable pageable
    );

    @Operation(summary = "패스워드 변경", description = "사용자의 패스워드를 변경합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "패스워드 변경 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"code\": \"200\", \"message\": \"패스워드 변경 성공\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "동일한 패스워드로 변경 요청",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": false, \"code\": \"400\", \"message\": \"기존과 동일한 비밀번호로 변경할 수 없습니다.\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "패스워드 인증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": false, \"code\": \"401\", \"message\": \"패스워드가 일치하지 않습니다.\", \"data\": null}"
                )
            )
        )
    })
    @PatchMapping("/me/password")
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<Void>> changePassword(
        @AuthenticationPrincipal Long userId,
        @RequestBody(
            description = "패스워드 변경 요청 데이터",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PasswordUpdateRequest.class),
                examples = @ExampleObject(
                    value = """
                        {
                            "currentPassword": "old_pass",
                            "newPassword": "new_pass"
                        }
                        """
                )
            )
        )
        @Valid @org.springframework.web.bind.annotation.RequestBody PasswordUpdateRequest request
    );

    @Operation(summary = "회원정보 수정", description = "회원의 이름, 이메일, 계좌, 주소 정보를 업데이트 합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "회원정보 수정 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"code\": \"200\", \"message\": \"회원정보 수정 성공\", \"data\": null}"
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
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 회원",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": false, \"code\": \"404\", \"message\": \"존재하지 않는 사용자입니다.\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "이미 가입된 이메일 주소",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": false, \"code\": \"409\", \"message\": \"이미 가입된 이메일 주소입니다.\", \"data\": null}"
                )
            )
        )
    })
    @PatchMapping("/me")
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<Void>> updateUserInfo(
        @AuthenticationPrincipal Long userId,
        @RequestBody(
            description = "회원정보 수정 요청 데이터<br/>이름, 이메일, 주소, 계좌 중 수정된 데이터만 요청",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserUpdateRequest.class),
                examples = @ExampleObject(
                    value = """
                        {
                            "email": "modified@email.com",
                            "account": "30242315606176"
                        }
                        """
                )
            )
        )
        @Valid @org.springframework.web.bind.annotation.RequestBody UserUpdateRequest request
    );
}
