package com.rentify.rentify_api.rental.exception;

import com.rentify.rentify_api.common.exception.NotFoundException;

public class RentalNotFoundException extends NotFoundException {
    public RentalNotFoundException() {
        super("대여 내역을 찾을 수 없습니다.");
    }
}
