package com.rentify.rentify_api.post.controller;

import com.rentify.rentify_api.post.dto.PostDetailResponse;
import com.rentify.rentify_api.post.dto.PostFormRequest;
import com.rentify.rentify_api.post.dto.PostFormResponse;
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
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Operation(summary = "전체 게시글 조회", description = "카테고리, 게시상태, 키워드에 따라 게시글을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "조건에 따른 게시글 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": true,
                          "code": "200",
                          "message": "요청이 성공적으로 처리되었습니다.",
                          "data": {
                            "content": [
                              {
                                "categoryName": "DSLR",
                                "createAt": "2026-02-01T15:49:40.686305",
                                "description": "DSLR 800D 대여합니다.",
                                "imageUrls": [
                                  "http://unirental.duckdns.org/images/dabdd601-72de-41b3-974d-f9e84f09a3dd.jpg",
                                  "http://unirental.duckdns.org/images/519a66a4-9927-4434-80ed-8e656fa4ed0a.jpg"
                                ],
                                "isMeetup": true,
                                "isParcel": false,
                                "maxRentalDays": 10,
                                "postId": 3,
                                "pricePerDay": 8000,
                                "status": "RESERVED",
                                "title": "DSLR 800D 대여",
                                "updateAt": "2026-02-01T15:51:45.984886",
                                "userId": 2,
                                "userName": "서성민굴"
                              }
                            ],
                            "page": {
                              "size": 10,
                              "number": 0,
                              "totalElements": 1,
                              "totalPages": 1
                            }
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "부적절한 파라미터<br/>정렬 기준의 경우 <strong>[createAt, pricePerDay, title, id]</strong> 지원",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "올바르지 않은 게시글 상태값 전달",
                        value = """
                            {
                                "success": false,
                                "code": "400",
                                "message": "유효하지 않은 게시글 상태입니다.",
                                "data": null
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "올바르지 않은 정렬기준 전달",
                        value = """
                            {
                                "success": false,
                                "code": "400",
                                "message": "정렬 기준 '[\\"string\\"]'은(는) 지원하지 않습니다. (허용 필드: [title, id, createAt, pricePerDay])",
                                "data": null
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "카테고리 ID 조회 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "success": false,
                            "code": "404",
                            "message": "등록되지 않은 카테고리입니다.",
                            "data": null
                        }
                        """
                )
            )
        )
    })
    @Parameters({
        @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0", in = ParameterIn.QUERY),
        @Parameter(name = "size", description = "한 페이지당 개수", example = "10", in = ParameterIn.QUERY),
        @Parameter(name = "sort", description = "정렬 기준 (형식: 필드명,정렬방향). 예: createAt,desc / pricePerDay,asc", example = "createAt,desc", in = ParameterIn.QUERY)
    })
    @GetMapping
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<Page<PostDetailResponse>>> getPosts(
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String status,

        @Parameter(
            description = "검색어 (제목 또는 내용)",
            example = "갤럭시"
        )
        @RequestParam(required = false) String keyword,

        @Parameter(hidden = true)
        @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable
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
                            "code": "200",
                            "message": "요청이 성공적으로 처리되었습니다.",
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
                            "code": "404",
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
                            "code": "201",
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
                                "code": "400",
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
                                "code": "400",
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
                                "code": "400",
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
                                "code": "404",
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
                                "code": "404",
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
                            "code": "409",
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
                            "code": "200",
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
                                "code": "400",
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
                                "code": "400",
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
                                "code": "404",
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
                                "code": "404",
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
                            "code": "403",
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
