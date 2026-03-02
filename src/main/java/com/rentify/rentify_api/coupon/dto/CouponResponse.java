package com.rentify.rentify_api.coupon.dto;

import com.rentify.rentify_api.coupon.entity.Coupon;
import com.rentify.rentify_api.coupon.entity.CouponDiscountType;
import com.rentify.rentify_api.coupon.entity.CouponStatus;
import java.time.LocalDateTime;

public record CouponResponse(
    Long couponId,
    String couponName,
    String description,
    CouponDiscountType discountType,
    Integer discountValue,
    Integer maxDiscountAmount,
    Integer minOrderAmount,
    Short totalQuantity,
    Short issuedQuantity,
    Short perUserLimit,
    LocalDateTime validFrom,
    LocalDateTime validUntil,
    CouponStatus status
) {
    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
            coupon.getId(),
            coupon.getCouponName(),
            coupon.getDescription(),
            coupon.getDiscountType(),
            coupon.getDiscountValue(),
            coupon.getMaxDiscountAmount(),
            coupon.getMinOrderAmount(),
            coupon.getTotalQuantity(),
            coupon.getIssuedQuantity(),
            coupon.getPerUserLimit(),
            coupon.getValidFrom(),
            coupon.getValidUntil(),
            coupon.getStatus()
        );
    }
}
