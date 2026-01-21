package com.rentify.rentify_api.image.exception;

import com.rentify.rentify_api.common.exception.FileException;

public class FileLimitExceededException extends FileException {

    public FileLimitExceededException() {
        super("파일은 최대 5개까지만 업로드 가능합니다.");
    }
}
