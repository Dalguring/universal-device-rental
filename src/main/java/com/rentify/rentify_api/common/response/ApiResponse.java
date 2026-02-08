package com.rentify.rentify_api.common.response;

import org.springframework.http.HttpStatus;

public record ApiResponse<T>(boolean success, String code, String message, T data) {

    // success
    public static <T> ApiResponse<T> success(HttpStatus status, T data) {
        return new ApiResponse<>(true, String.valueOf(status.value()), "요청이 성공적으로 처리되었습니다.", data);
    }

    public static <T> ApiResponse<T> success(HttpStatus status, String message, T data) {
        return new ApiResponse<>(true, String.valueOf(status.value()), message, data);
    }

    public static ApiResponse<Void> success(HttpStatus status) {
        return new ApiResponse<>(true, String.valueOf(status.value()), "요청이 성공적으로 처리되었습니다.", null);
    }

    public static ApiResponse<Void> success(HttpStatus status, String message) {
        return new ApiResponse<>(true, String.valueOf(status.value()), message, null);
    }

    // error
    public static ApiResponse<Void> error(HttpStatus status, String message) {
        return new ApiResponse<>(false, String.valueOf(status.value()), message, null);
    }
}
