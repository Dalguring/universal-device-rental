package com.rentify.rentify_api.coupon.controller;

import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.coupon.dto.CouponResponse;
import com.rentify.rentify_api.coupon.service.CouponService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController implements CouponApiDocs {

    private final CouponService couponService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAllCoupons() {
        List<CouponResponse> coupons = couponService.getAllCoupons();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, coupons));
    }
}
