package com.rentify.rentify_api.coupon.exception;

import com.rentify.rentify_api.common.exception.InvalidValueException;

public class CouponNotValidException extends InvalidValueException {

    public CouponNotValidException() {
        super("쿠폰 유효 기간이 아닙니다.");
    }
}
