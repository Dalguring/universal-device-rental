package com.rentify.rentify_api.user.exception;

import com.rentify.rentify_api.common.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException() {
        super("존재하지 않는 사용자입니다.");
    }
}
