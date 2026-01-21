package com.rentify.rentify_api.image.exception;

import com.rentify.rentify_api.common.exception.FileException;

public class FileSizeExceededException extends FileException {

    public FileSizeExceededException() {
        super("최대 10MB 까지만 업로드 가능합니다.");
    }
}
