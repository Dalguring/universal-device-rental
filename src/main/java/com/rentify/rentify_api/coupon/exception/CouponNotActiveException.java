package com.rentify.rentify_api.coupon.exception;

import com.rentify.rentify_api.common.exception.InvalidValueException;

public class CouponNotActiveException extends InvalidValueException {

    public CouponNotActiveException() {
        super("발급 가능한 쿠폰이 아닙니다.");
    }
}
