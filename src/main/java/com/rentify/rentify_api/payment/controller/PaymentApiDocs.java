package com.rentify.rentify_api.payment.controller;

import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.payment.dto.PaymentDetailResponse;
import com.rentify.rentify_api.payment.dto.PaymentRequest;
import com.rentify.rentify_api.payment.dto.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Payment API", description = "결제 API")
public interface PaymentApiDocs {

    @Operation(summary = "결제요청", description = "<strong>멱등성 키(UUID) 헤더 필수</strong><br/>결제를 요청합니다."
        + "<br/>pointAmount: 사용 포인트<br/>expectedAmount: 쿠폰 및 포인트 사용 후 프론트엔드 계산 결과 최종 결제금액"
        + "<br/><strong>33% 확률로 실패할 수 있습니다.</strong>")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "결제 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                           "success": true,
                           "code": "200",
                           "message": "결제가 완료되었습니다.",
                           "data": {
                               "paymentId": 1
                           }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "대여 불가능(대여 요청 상태가 아님)",
                        value = "{\"success\": false, \"code\": \"400\", \"message\": \"대여 불가능한 상태입니다.\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "이미 사용된 쿠폰",
                        value = "{\"success\": false, \"code\": \"400\", \"message\": \"이미 사용된 쿠폰입니다.\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "쿠폰 유효기간 오류",
                        value = "{\"success\": false, \"code\": \"400\", \"message\": \"쿠폰 유효 기간이 아닙니다.\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "사용 가능 포인트 초과",
                        value = "{\"success\": false, \"code\": \"400\", \"message\": \"사용 가능한 포인트를 초과했습니다.\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "쿠폰 최소주문금액 미달",
                        value = "{\"success\": false, \"code\": \"400\", \"message\": \"주문 금액이 쿠폰 최소 주문 금액보다 작습니다.\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "결제 요청 금액 변조",
                        value = "{\"success\": false, \"code\": \"400\", \"message\": \"결제 요청 금액이 변조되었거나 일치하지 않습니다.\", \"data\": null}"
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "권한 없음",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "대여자와 결제자 불일치",
                        value = "{\"success\": false, \"code\": \"401\", \"message\": \"대여자와 결제 요청자가 일치하지 않습니다.\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "사용자의 쿠폰 미소유",
                        value = "{\"success\": false, \"code\": \"401\", \"message\": \"사용자가 소유하지 않은 쿠폰입니다.\", \"data\": null}"
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 데이터",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 대여 데이터",
                        value = "{\"success\": false, \"code\": \"404\", \"message\": \"대여 내역을 찾을 수 없습니다.\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "존재하지 않는 쿠폰 데이터",
                        value = "{\"success\": false, \"code\": \"404\", \"message\": \"쿠폰을 찾을 수 없습니다.\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "존재하지 않는 결제 데이터",
                        value = "{\"success\": false, \"code\": \"404\", \"message\": \"결제 내역이 없습니다.\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "존재하지 않는 게시 데이터",
                        value = "{\"success\": false, \"code\": \"404\", \"message\": \"게시글을 찾을 수 없습니다.\", \"data\": null}"
                    ),
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "이미 대여된 물품",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": false, \"code\": \"409\", \"message\": \"물품이 이미 대여되었습니다. 결제가 취소됩니다.\", \"data\": null}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "결제 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": false, \"code\": \"500\", \"message\": \"결제 승인에 실패했습니다.\", \"data\": null}"
                )
            )
        )
    })
    @Parameter(
        name = "Idempotency-Key",
        description = "중복 요청 방지를 위한 멱등성 키",
        required = true,
        in = ParameterIn.HEADER,
        schema = @Schema(type = "string", format = "uuid")
    )
    @PostMapping
    ResponseEntity<ApiResponse<PaymentResponse>> requestPayment(
        @AuthenticationPrincipal Long userId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "결제 요청 데이터",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaymentRequest.class),
                examples = @ExampleObject(
                    value = """
                        {
                            "rentalId": 1,
                            "userCouponId": 2,
                            "pointAmount": 1000,
                            "expectedAmount": 27000
                        }
                        """
                )
            )
        )
        @Valid @RequestBody PaymentRequest request
    );

    @Operation(summary = "회원 전체 결제 내역 조회", description = "로그인 된 회원의 전체 결제 내역을 조회합니다.<br/>"
        + "status = PENDING(결제 요청), PAID(결제 완료), FAILED(결제 실패), CANCELED(결제 취소), REFUNDED(환불 완료)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "전체 결제 내역 조회 성공",
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
                                 "cancelAt": null,
                                 "couponDiscount": 5000,
                                 "createAt": "2026-03-18T17:16:19.69493",
                                 "failReason": null,
                                 "finalAmount": 52000,
                                 "paidAt": "2026-03-18T17:16:20.947806",
                                 "paymentId": 1,
                                 "refundAt": null,
                                 "rentalId": 35,
                                 "status": "PAID",
                                 "totalAmount": 60000,
                                 "usedPoint": 3000,
                                 "userCouponId": 7
                               }
                             ],
                             "page": {
                               "size": 1,
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "success": false,
                            "code": "500",
                            "message": "System error message",
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
        @Parameter(name = "sort", description = "정렬 기준 (createAt,desc 고정)", example = "createAt,desc", in = ParameterIn.QUERY)
    })
    @GetMapping
    ResponseEntity<ApiResponse<Page<PaymentDetailResponse>>> getPaymentsInfo(
        @AuthenticationPrincipal Long userId,
        @Parameter(hidden = true)
        @PageableDefault(sort = "createAt", direction = Direction.DESC) Pageable pageable
    );

    @Operation(summary = "결제 내역 조회", description = "특정 결제 내역을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "결제 내역 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "success": true,
                            "code": "200",
                            "message": "요청이 성공적으로 처리되었습니다.",
                            "data": {
                              "cancelAt": null,
                              "couponDiscount": 5000,
                              "createAt": "2026-03-18T17:16:19.69493",
                              "failReason": null,
                              "finalAmount": 52000,
                              "paidAt": "2026-03-18T17:16:20.947806",
                              "paymentId": 1,
                              "refundAt": null,
                              "rentalId": 35,
                              "status": "PAID",
                              "totalAmount": 60000,
                              "usedPoint": 3000,
                              "userCouponId": 7
                            }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "본인 결제 내역 외 조회 불가",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "success": false,
                            "code": "401",
                            "message": "본인의 결제 건만 조회할 수 있습니다.",
                            "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "결제 내역 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "success": false,
                            "code": "404",
                            "message": "결제 내역이 없습니다.",
                            "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "success": false,
                            "code": "500",
                            "message": "System error message",
                            "data": null
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<PaymentDetailResponse>> getPaymentInfo(
        @AuthenticationPrincipal Long userId,
        @Parameter(
            name = "id",
            description = "결제 ID",
            required = true,
            in = ParameterIn.PATH,
            example = "1"
        )
        @PathVariable Long id
    );

    @Operation(summary = "결제 취소 요청", description = "<strong>멱등성 키(UUID) 헤더 필수</strong><br/>결제 취소를 요청합니다."
        + "<br/><strong>100% 확률로 성공</strong>")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "결제 취소 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                           "success": true,
                           "code": "200",
                           "message": "결제 취소가 완료되었습니다.",
                           "data": {
                               "paymentId": 1
                           }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "결제 완료 건 외 취소 불가",
                        value = "{\"success\": false, \"code\": \"400\", \"message\": \"결제 완료 상태에서만 취소할 수 있습니다.\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "대여 시작일 이후 결제 취소 불가",
                        value = "{\"success\": false, \"code\": \"400\", \"message\": \"대여 시작일 이후에는 결제를 취소할 수 없습니다.\", \"data\": null}"
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "권한 없음",
            content = @Content(
                mediaType = "application/json",
                examples =
                @ExampleObject(
                    name = "본인 결제 건 외 취소 불가",
                    value = "{\"success\": false, \"code\": \"401\", \"message\": \"본인의 결제 건만 취소할 수 있습니다.\", \"data\": null}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 데이터",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "존재하지 않는 결제 데이터",
                    value = "{\"success\": false, \"code\": \"404\", \"message\": \"결제 내역이 없습니다.\", \"data\": null}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": false, \"code\": \"500\", \"message\": \"서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.\", \"data\": null}"
                )
            )
        )
    })
    @Parameter(
        name = "Idempotency-Key",
        description = "중복 요청 방지를 위한 멱등성 키",
        required = true,
        in = ParameterIn.HEADER,
        schema = @Schema(type = "string", format = "uuid")
    )
    @PostMapping("/{id}/cancel")
    ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
        @AuthenticationPrincipal Long userId,
        @Parameter(
            name = "id",
            description = "결제 ID",
            required = true,
            in = ParameterIn.PATH,
            example = "1"
        )
        @PathVariable Long id
    );
}
