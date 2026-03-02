package com.rentify.rentify_api.coupon.exception;

import com.rentify.rentify_api.common.exception.InvalidValueException;

public class CouponSoldOutException extends InvalidValueException {

    public CouponSoldOutException() {
        super("쿠폰이 모두 소진되었습니다.");
    }
}
