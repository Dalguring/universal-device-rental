package com.rentify.rentify_api.image.exception;

import com.rentify.rentify_api.common.exception.FileException;

public class FileTypeNotAllowedException extends FileException {

    public FileTypeNotAllowedException() {
        super("지원하지 않는 파일 형식입니다.");
    }
}
