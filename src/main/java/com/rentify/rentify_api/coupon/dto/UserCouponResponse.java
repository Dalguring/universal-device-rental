package com.rentify.rentify_api.coupon.dto;

import com.rentify.rentify_api.coupon.entity.CouponDiscountType;
import com.rentify.rentify_api.coupon.entity.UserCoupon;
import com.rentify.rentify_api.coupon.entity.UserCouponStatus;
import java.time.LocalDateTime;

public record UserCouponResponse(
    Long userCouponId,
    Long couponId,
    String couponName,
    String description,
    CouponDiscountType discountType,
    Integer discountValue,
    Integer maxDiscountAmount,
    Integer minOrderAmount,
    LocalDateTime validFrom,
    LocalDateTime validUntil,
    LocalDateTime issuedAt,
    LocalDateTime usedAt,
    UserCouponStatus status
) {
    public static UserCouponResponse from(UserCoupon userCoupon) {
        return new UserCouponResponse(
            userCoupon.getId(),
            userCoupon.getCoupon().getId(),
            userCoupon.getCoupon().getCouponName(),
            userCoupon.getCoupon().getDescription(),
            userCoupon.getCoupon().getDiscountType(),
            userCoupon.getCoupon().getDiscountValue(),
            userCoupon.getCoupon().getMaxDiscountAmount(),
            userCoupon.getCoupon().getMinOrderAmount(),
            userCoupon.getCoupon().getValidFrom(),
            userCoupon.getCoupon().getValidUntil(),
            userCoupon.getIssuedAt(),
            userCoupon.getUsedAt(),
            userCoupon.getStatus()
        );
    }
}
