package com.rentify.rentify_api.rental.exception;

import com.rentify.rentify_api.common.exception.InvalidValueException;

public class RentalNotAvailableException extends InvalidValueException {
    public RentalNotAvailableException(String message) {
        super(message);
    }
}
