package com.rentify.rentify_api.post.controller;

import com.rentify.rentify_api.post.dto.PostDetailResponse;
import com.rentify.rentify_api.post.dto.PostFormRequest;
import com.rentify.rentify_api.post.dto.PostFormResponse;
import com.rentify.rentify_api.post.entity.PostStatus;
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
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Post API", description = "게시글 조회, 생성, 수정 관련 API")
public interface PostApiDocs {

    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<Void>> getPosts(
        @RequestParam Long categoryId,
        @RequestParam PostStatus status,
        @RequestParam String keyword,
        @PageableDefault Pageable pageable
    );

    @Operation(summary = "게시글 상세 조회", description = "게시글을 상세 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "게시글 상세 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "success": true,
                            "code": "SUCCESS",
                            "message": "요청이 성공했습니다.",
                            "data": {
                                "categoryName": "갤럭시 울트라",
                                "createAt": "2026-01-28T20:55:58.522954",
                                "description": "테스트입니다",
                                "imageUrls": [
                                "http://unirental.duckdns.org/images/6d82b234-6708-4875-ba60-80af76cc9e69.jpg",
                                "http://unirental.duckdns.org/images/20689993-ef98-4919-b8e9-631448253749.jpg"
                                ],
                                "isMeetup": false,
                                "isParcel": true,
                                "maxRentalDays": 30,
                                "postId": 1,
                                "pricePerDay": 50000,
                                "status": "AVAILABLE",
                                "title": "갤럭시 S25엣지 대여",
                                "updateAt": "2026-01-28T20:55:58.523022",
                                "userId": 1,
                                "userName": "서성민"
                            }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 게시글",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "success": false,
                            "code": "NOT_FOUND",
                            "message": "게시글을 찾을 수 없습니다.",
                            "data": null
                        }
                        """
                )
            )
        )
    })
    @Parameter(
        name = "id",
        description = "게시글 ID",
        required = true,
        in = ParameterIn.PATH,
        example = "1"
    )
    @GetMapping("{id}")
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<PostDetailResponse>> getPost(
        @PathVariable Long id);

    @Operation(summary = "게시글 생성", description = "<strong>멱등성 키(UUID) 헤더 필수</strong><br/>게시글을 등록합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "게시글 생성 성공",
            headers = @Header(
                name = HttpHeaders.LOCATION,
                description = "생성된 게시글 ID URI",
                schema = @Schema(type = "string", example = "/api/posts/12")
            ),
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "success": true,
                            "code": "SUCCESS",
                            "message": "게시글이 생성되었습니다.",
                            "data": {
                                "postId": 12
                            }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "요청 데이터 검증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "카테고리 ID 필수",
                        value = """
                            {
                                "success": false,
                                "code": "INVALID_REQUEST",
                                "message": "categoryId : Category ID is required",
                                "data": null
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "제목 필수",
                        value = """
                            {
                                "success": false,
                                "code": "INVALID_REQUEST",
                                "message": "title : Title is required",
                                "data": null
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "지원하지 않는 파일 형식",
                        value = """
                            {
                                "success": false,
                                "code": "FILE_EXCEPTION",
                                "message": "지원하지 않는 파일 형식입니다.",
                                "data": null
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 데이터를 파라미터로 전달",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "사용자 조회 실패",
                        summary = "존재하지 않는 사용자",
                        value = """
                            {
                                "success": false,
                                "code": "NOT_FOUND",
                                "message": "존재하지 않는 사용자입니다.",
                                "data": null
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "카테고리 ID 조회 실패",
                        summary = "존재하지 않는 카테고리",
                        value = """
                            {
                                "success": false,
                                "code": "NOT_FOUND",
                                "message": "등록되지 않은 카테고리입니다.",
                                "data": null
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "게시물 등록 처리 중",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    summary = "중복된 요청(멱등성 처리)",
                    value = """
                        {
                            "success": false,
                            "code": "PROCESS_IN_PROGRESS",
                            "message": "이전 게시글이 생성 중 입니다. 잠시 후 결과를 확인해주세요.",
                            "data": null
                        }
                        """
                )
            )
        )
    })
    @PostMapping
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<PostFormResponse>> createPost(
        @Parameter(
            name = "Idempotency-Key",
            description = "중복 요청 방지를 위한 멱등성 키",
            required = true,
            in = ParameterIn.HEADER,
            example = "123e4567-e89b-12d3-a456-426614174000"
        )
        @RequestHeader(value = "Idempotency-Key") UUID idempotencyKey,
        @AuthenticationPrincipal Long userId,
        @RequestBody(
            description = "게시글 생성 요청 데이터",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PostFormRequest.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "categoryId": 1,
                        "title": "title",
                        "description": "description",
                        "pricePerDay": 50000,
                        "maxRentalDays": 30,
                        "isParcel": true,
                        "isMeetup": false,
                        "imageUrls": ["http://backend.server.ip/images/1.jpg", "http://backend.server.ip/images/1.png"]
                    }
                    """
                )
            )
        )
        @Valid PostFormRequest request
    );

    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다.<br/><strong>수정된 전체 내용 전송 필수</strong>")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "게시글 수정 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "success": true,
                            "code": "SUCCESS",
                            "message": "게시글 수정 성공",
                            "data": {
                                "postId": 1
                            }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "요청 데이터 검증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "카테고리 ID 필수",
                        value = """
                            {
                                "success": false,
                                "code": "INVALID_REQUEST",
                                "message": "categoryId : Category ID is required",
                                "data": null
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "제목 필수",
                        value = """
                            {
                                "success": false,
                                "code": "INVALID_REQUEST",
                                "message": "title : Title is required",
                                "data": null
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 데이터를 파라미터로 전달",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "게시글 조회 실패",
                        summary = "존재하지 않는 게시글",
                        value = """
                            {
                                "success": false,
                                "code": "NOT_FOUND",
                                "message": "게시글을 찾을 수 없습니다.",
                                "data": null
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "카테고리 ID 조회 실패",
                        summary = "존재하지 않는 카테고리",
                        value = """
                            {
                                "success": false,
                                "code": "NOT_FOUND",
                                "message": "등록되지 않은 카테고리입니다.",
                                "data": null
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "수정 권한 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "success": false,
                            "code": "ACCESS_DENIED",
                            "message": "수정 권한이 없습니다.",
                            "data": null
                        }
                        """
                )
            )
        )
    })
    @PutMapping("{id}")
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<PostFormResponse>> updatePost(
        @Parameter(
            name = "id",
            description = "게시글 ID",
            required = true,
            in = ParameterIn.PATH,
            example = "1"
        )
        @PathVariable Long id,
        @AuthenticationPrincipal Long userId,
        @RequestBody(
            description = "게시글 수정 요청 데이터",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PostFormRequest.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "categoryId": 1,
                        "title": "title",
                        "description": "description",
                        "pricePerDay": 50000,
                        "maxRentalDays": 30,
                        "isParcel": true,
                        "isMeetup": false,
                        "status": "RESERVED",
                        "imageUrls": ["http://backend.server.ip/images/1.jpg", "http://backend.server.ip/images/1.png"]
                    }
                    """
                )
            )
        )
        @Valid @org.springframework.web.bind.annotation.RequestBody PostFormRequest request
    );
}
