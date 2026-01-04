package com.rentify.rentify_api.user.exception;

import com.rentify.rentify_api.common.exception.DuplicateException;

public class DuplicateEmailException extends DuplicateException {

    public DuplicateEmailException() {
        super("이미 가입된 이메일 주소입니다.");
    }
}
