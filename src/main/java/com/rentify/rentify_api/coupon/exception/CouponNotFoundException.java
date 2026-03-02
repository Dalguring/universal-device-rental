package com.rentify.rentify_api.coupon.exception;

import com.rentify.rentify_api.common.exception.NotFoundException;

public class CouponNotFoundException extends NotFoundException {

    public CouponNotFoundException() {
        super("쿠폰을 찾을 수 없습니다.");
    }
}
