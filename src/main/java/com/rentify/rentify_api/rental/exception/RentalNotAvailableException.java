package com.rentify.rentify_api.rental.exception;

import com.rentify.rentify_api.common.exception.InvalidValueException;

public class RentalNotAvailableException extends InvalidValueException {

    public RentalNotAvailableException() {
        super("대여 불가능한 상태입니다.");
    }

    public RentalNotAvailableException(String message) {
        super(message);
    }
}
