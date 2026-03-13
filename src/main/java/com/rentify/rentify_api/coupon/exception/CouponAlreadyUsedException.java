package com.rentify.rentify_api.coupon.exception;

import com.rentify.rentify_api.common.exception.InvalidValueException;

public class CouponAlreadyUsedException extends InvalidValueException {

    public CouponAlreadyUsedException() {
        super("이미 사용된 쿠폰입니다.");
    }
}
