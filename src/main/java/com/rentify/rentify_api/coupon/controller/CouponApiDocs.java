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
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name = "Coupon API", description = "쿠폰 API")
public interface CouponApiDocs {

    @Operation(
        summary = "모든 쿠폰 조회",
        description = "사이트에 등록된 모든 쿠폰 목록을 조회합니다. 인증 없이 조회 가능합니다."
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
                              "couponId": 2,
                              "couponName": "봄맞이 선착순 5천원 할인",
                              "description": "선착순 100명 한정 봄맞이 특별 할인",
                              "discountType": "FIXED",
                              "discountValue": 5000,
                              "maxDiscountAmount": null,
                              "minOrderAmount": 30000,
                              "totalQuantity": 100,
                              "issuedQuantity": 98,
                              "perUserLimit": 1,
                              "validFrom": "2026-03-01T00:00:00",
                              "validUntil": "2026-03-31T23:59:59",
                              "status": "ACTIVE"
                            },
                            {
                              "couponId": 3,
                              "couponName": "2025년 크리스마스 특가",
                              "description": "크리스마스 기념 20% 할인 (종료)",
                              "discountType": "PERCENT",
                              "discountValue": 20,
                              "maxDiscountAmount": 10000,
                              "minOrderAmount": 50000,
                              "totalQuantity": 500,
                              "issuedQuantity": 500,
                              "perUserLimit": 2,
                              "validFrom": "2025-12-01T00:00:00",
                              "validUntil": "2025-12-31T23:59:59",
                              "status": "ENDED"
                            },
                            {
                              "couponId": 1,
                              "couponName": "신규 가입 환영 10% 할인 쿠폰",
                              "description": "신규 가입자를 위한 상시 발급 쿠폰 (최대 5천원 할인)",
                              "discountType": "PERCENT",
                              "discountValue": 10,
                              "maxDiscountAmount": 5000,
                              "minOrderAmount": 10000,
                              "totalQuantity": 30000,
                              "issuedQuantity": 1501,
                              "perUserLimit": 1,
                              "validFrom": "2026-01-01T00:00:00",
                              "validUntil": "2099-12-31T23:59:59",
                              "status": "ACTIVE"
                            }
                          ]
                        }
                        """
                )
            )
        )
    })
    @GetMapping
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<List<CouponResponse>>> getAllCoupons();

}
