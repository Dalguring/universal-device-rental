package com.rentify.rentify_api.rental.controller;

import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.rental.dto.RentalRequest;
import com.rentify.rentify_api.rental.dto.RentalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Rental API", description = "대여 API")
public interface RentalApiDocs {

    @Operation(
        summary = "대여 신청",
        description = "게시글에 대한 대여를 신청합니다.<br/>" +
                     "이후 결제 확정 API를 호출하여 대여를 확정해야 합니다.<br/><br/>" +
                     "대여 상태: <strong>REQUESTED</strong><br/>" +
                     "게시글 상태: 변경 없음"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "대여 신청 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "success": true,
                            "code": "SUCCESS",
                            "message": "요청이 성공적으로 처리되었습니다.",
                            "data": {
                                "rentalId": 1,
                                "userId": 1,
                                "postId": 5,
                                "startDate": "2026-02-10",
                                "endDate": "2026-02-15",
                                "receiveMethod": "PARCEL",
                                "status": "REQUESTED",
                                "totalPrice": 250000,
                                "createdAt": "2026-02-08T10:30:00",
                                "updatedAt": "2026-02-08T10:30:00"
                            }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "요청 데이터 검증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "필수 필드 누락",
                        value = """
                            {
                                "success": false,
                                "code": "INVALID_REQUEST",
                                "message": "postId : 게시글 ID는 필수입니다.",
                                "data": null
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "대여 시작일 유효성 검증 실패",
                        value = """
                            {
                                "success": false,
                                "code": "INVALID_VALUE",
                                "message": "대여 시작일은 오늘 이후여야 합니다.",
                                "data": null
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "대여 기간 중복",
                        value = """
                            {
                                "success": false,
                                "code": "INVALID_VALUE",
                                "message": "해당 기간에 이미 대여가 진행 중이거나 예정되어 있습니다.",
                                "data": null
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "최대 대여 기간 초과",
                        value = """
                            {
                                "success": false,
                                "code": "INVALID_VALUE",
                                "message": "최대 대여 기간(30일)을 초과했습니다.",
                                "data": null
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "수령 방법 불가",
                        value = """
                            {
                                "success": false,
                                "code": "INVALID_VALUE",
                                "message": "택배 수령이 불가능한 게시글입니다.",
                                "data": null
                            }
                            """
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "게시글 또는 사용자를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "게시글 조회 실패",
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
                        name = "사용자 조회 실패",
                        value = """
                            {
                                "success": false,
                                "code": "NOT_FOUND",
                                "message": "사용자를 찾을 수 없습니다.",
                                "data": null
                            }
                            """
                    )
                }
            )
        )
    })
    @PostMapping
    ResponseEntity<ApiResponse<RentalResponse>> createRental(
        @Parameter(hidden = true)
        @AuthenticationPrincipal Long userId,
        
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "대여 신청 요청 데이터",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RentalRequest.class),
                examples = @ExampleObject(
                    value = """
                        {
                            "postId": 5,
                            "startDate": "2026-02-10",
                            "endDate": "2026-02-15",
                            "receiveMethod": "PARCEL"
                        }
                        """
                )
            )
        )
        @Valid @RequestBody RentalRequest request
    );

    @Operation(
        summary = "대여 확정 (결제 완료 시)",
        description = "대여 신청 후 결제가 완료되면 호출하여 대여를 확정합니다.<br/><br/>" +
                     "대여 상태: <strong>REQUESTED → CONFIRMED</strong><br/>" +
                     "게시글 상태: <strong>AVAILABLE → RESERVED</strong>"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "대여 확정 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "success": true,
                            "code": "SUCCESS",
                            "message": "요청이 성공적으로 처리되었습니다.",
                            "data": {
                                "rentalId": 1,
                                "userId": 1,
                                "postId": 5,
                                "startDate": "2026-02-10",
                                "endDate": "2026-02-15",
                                "receiveMethod": "PARCEL",
                                "status": "CONFIRMED",
                                "totalPrice": 250000,
                                "createdAt": "2026-02-08T10:30:00",
                                "updatedAt": "2026-02-08T10:35:00"
                            }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "대여 확정 불가",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "본인의 대여가 아님",
                        value = """
                            {
                                "success": false,
                                "code": "INVALID_VALUE",
                                "message": "본인의 대여만 확정할 수 있습니다.",
                                "data": null
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "확정 불가능한 상태",
                        value = """
                            {
                                "success": false,
                                "code": "INVALID_STATE",
                                "message": "신청 상태에서만 확정할 수 있습니다.",
                                "data": null
                            }
                            """
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "대여 정보를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "success": false,
                            "code": "NOT_FOUND",
                            "message": "대여 정보를 찾을 수 없습니다.",
                            "data": null
                        }
                        """
                )
            )
        )
    })
    @PatchMapping("/{rentalId}/confirm")
    ResponseEntity<ApiResponse<RentalResponse>> confirmRental(
        @Parameter(hidden = true)
        @AuthenticationPrincipal Long userId,
        
        @Parameter(
            name = "rentalId",
            description = "대여 ID",
            required = true,
            in = ParameterIn.PATH,
            example = "1"
        )
        @PathVariable Long rentalId
    );

    @Operation(
        summary = "대여 취소",
        description = "대여를 취소합니다.<br/><br/>" +
                     "대여 상태: <strong>REQUESTED/CONFIRMED → CANCELED</strong><br/>" +
                     "게시글 상태: <strong>RESERVED → AVAILABLE</strong> (예약 상태였을 경우에만)<br/>" +
                     "취소 조건: 대여 시작일 이전에만 가능 <br/>"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "대여 취소 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "success": true,
                            "code": "SUCCESS",
                            "message": "요청이 성공적으로 처리되었습니다.",
                            "data": {
                                "rentalId": 1,
                                "userId": 1,
                                "postId": 5,
                                "startDate": "2026-02-10",
                                "endDate": "2026-02-15",
                                "receiveMethod": "PARCEL",
                                "status": "CANCELED",
                                "totalPrice": 250000,
                                "createdAt": "2026-02-08T10:30:00",
                                "updatedAt": "2026-02-08T10:40:00"
                            }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "대여 취소 불가",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "본인의 대여가 아님",
                        value = """
                            {
                                "success": false,
                                "code": "INVALID_VALUE",
                                "message": "본인의 대여만 취소할 수 있습니다.",
                                "data": null
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "취소 불가능한 상태",
                        value = """
                            {
                                "success": false,
                                "code": "INVALID_STATE",
                                "message": "신청 또는 확정 상태에서만 취소할 수 있습니다.",
                                "data": null
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "대여 시작일 이후 취소 시도",
                        value = """
                            {
                                "success": false,
                                "code": "INVALID_STATE",
                                "message": "대여 시작일 이후에는 취소할 수 없습니다.",
                                "data": null
                            }
                            """
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "대여 정보를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "success": false,
                            "code": "NOT_FOUND",
                            "message": "대여 정보를 찾을 수 없습니다.",
                            "data": null
                        }
                        """
                )
            )
        )
    })
    @PatchMapping("/{rentalId}/cancel")
    ResponseEntity<ApiResponse<RentalResponse>> cancelRental(
        @Parameter(hidden = true)
        @AuthenticationPrincipal Long userId,
        
        @Parameter(
            name = "rentalId",
            description = "대여 ID",
            required = true,
            in = ParameterIn.PATH,
            example = "1"
        )
        @PathVariable Long rentalId
    );
}
