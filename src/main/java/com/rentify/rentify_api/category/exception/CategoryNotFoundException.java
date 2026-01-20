package com.rentify.rentify_api.category.exception;

import com.rentify.rentify_api.common.exception.NotFoundException;

public class CategoryNotFoundException extends NotFoundException {

    public CategoryNotFoundException() {
        super("등록되지 않은 카테고리입니다.");
    }
}
