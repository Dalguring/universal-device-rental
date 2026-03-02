package com.rentify.rentify_api.coupon.controller;

import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.coupon.dto.IssueCouponRequest;
import com.rentify.rentify_api.coupon.dto.UserCouponResponse;
import com.rentify.rentify_api.coupon.service.CouponService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@Slf4j
@RequestMapping("/api/users/me/coupons")
@RequiredArgsConstructor
public class UserCouponController implements UserCouponApiDocs {

    private final CouponService couponService;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> issueCoupon(
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody IssueCouponRequest request
    ) {
        couponService.issueCoupon(userId, request.getCouponId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(HttpStatus.CREATED, "쿠폰 발급 성공"));
    }

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserCouponResponse>>> getMyUserCoupons(
        @AuthenticationPrincipal Long userId
    ) {
        List<UserCouponResponse> userCoupons = couponService.getMyUserCoupons(userId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, userCoupons));
    }
}
