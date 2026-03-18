package com.rentify.rentify_api.post.exception;

import com.rentify.rentify_api.common.exception.NotFoundException;

public class PostNotFoundException extends NotFoundException {

    public PostNotFoundException() {
        super("게시글을 찾을 수 없습니다.");
    }
}
