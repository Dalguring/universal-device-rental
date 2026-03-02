package com.rentify.rentify_api.coupon.exception;

import com.rentify.rentify_api.common.exception.InvalidValueException;

public class CouponIssueLimitExceededException extends InvalidValueException {

    public CouponIssueLimitExceededException() {
        super("쿠폰 발급 가능 수량을 초과했습니다.");
    }
}
