package com.rentify.rentify_api.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    // success
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "SUCCESS", "요청이 성공적으로 처리되었습니다.", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, "SUCCESS", message, data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, "SUCCESS", "요청이 성공적으로 처리되었습니다.", null);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, "SUCCESS", message, null);
    }

    // error
    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }
}
