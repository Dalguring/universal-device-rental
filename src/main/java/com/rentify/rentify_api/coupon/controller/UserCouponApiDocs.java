package com.rentify.rentify_api.coupon.controller;

import com.rentify.rentify_api.coupon.dto.CouponResponse;
import com.rentify.rentify_api.coupon.dto.IssueCouponRequest;
import com.rentify.rentify_api.coupon.dto.UserCouponResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Tag(name = "Coupon API", description = "쿠폰 API")
public interface UserCouponApiDocs {

    @Operation(
        summary = "쿠폰 발급",
        description = """
            현재 로그인한 사용자가 쿠폰을 발급받습니다.
            
            ### 발급 조건:
            1. 현재 발급수량 + 1 ≤ 총 발급수량
            2. 유저가 발급받은 수량 < 유저당 제한수량
            3. 현재 시각이 유효기간(valid_from ~ valid_until) 이내
            4. 쿠폰 상태가 ACTIVE
            
            ### 발급 처리:
            - 쿠폰의 issued_quantity +1 증가
            - 총 발급수량에 도달 시 쿠폰 상태를 INACTIVE로 변경
            - user_coupons 테이블에 발급 이력 생성
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "쿠폰 발급 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"code\": \"201\", \"message\": \"쿠폰 발급 성공\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "발급 조건 미충족",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "쿠폰 소진",
                        value = "{\"success\": false, \"code\": \"400\", \"message\": \"쿠폰이 모두 소진되었습니다.\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "발급 수량 초과",
                        value = "{\"success\": false, \"code\": \"400\", \"message\": \"쿠폰 발급 가능 수량을 초과했습니다.\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "유효기간 아님",
                        value = "{\"success\": false, \"code\": \"400\", \"message\": \"쿠폰 유효 기간이 아닙니다.\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "쿠폰 비활성",
                        value = "{\"success\": false, \"code\": \"400\", \"message\": \"발급 가능한 쿠폰이 아닙니다.\", \"data\": null}"
                    )
                }
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
        ),
        @ApiResponse(
            responseCode = "404",
            description = "쿠폰을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": false, \"code\": \"404\", \"message\": \"쿠폰을 찾을 수 없습니다.\", \"data\": null}"
                )
            )
        )
    })
    @PostMapping
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<Void>> issueCoupon(
        @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
        @RequestBody(
            description = "쿠폰 발급 요청 데이터",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = IssueCouponRequest.class),
                examples = @ExampleObject(
                    value = "{\"couponId\": 1}"
                )
            )
        )
        @Valid @org.springframework.web.bind.annotation.RequestBody IssueCouponRequest request
    );

    @Operation(
        summary = "내 쿠폰 조회",
        description = "현재 로그인한 사용자가 발급받은 모든 쿠폰 목록을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": true,
                          "code": "200",
                          "message": "요청이 성공적으로 처리되었습니다.",
                          "data": [
                            {
                              "userCouponId": 1,
                              "couponId": 1,
                              "couponName": "신규 가입 환영 10% 할인 쿠폰",
                              "description": "신규 가입자를 위한 상시 발급 쿠폰 (최대 5천원 할인)",
                              "discountType": "PERCENT",
                              "discountValue": 10,
                              "maxDiscountAmount": 5000,
                              "minOrderAmount": 10000,
                              "validFrom": "2026-01-01T00:00:00",
                              "validUntil": "2099-12-31T23:59:59",
                              "issuedAt": "2026-03-02T15:27:57.080995",
                              "usedAt": null,
                              "status": "AVAILABLE"
                            }
                          ]
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
    @GetMapping
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<List<UserCouponResponse>>> getMyUserCoupons(
        @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );
}
