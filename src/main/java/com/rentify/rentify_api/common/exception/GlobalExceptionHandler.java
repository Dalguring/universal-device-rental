package com.rentify.rentify_api.common.exception;

import com.rentify.rentify_api.common.response.ApiResponse;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception ex,
        @Nullable Object body,
        @NonNull HttpHeaders headers,
        HttpStatusCode statusCode,
        @NonNull WebRequest request
    ) {
        log.warn("Spring Standard Exception: {} - {}",
            ex.getClass().getSimpleName(), ex.getMessage()
        );

        ApiResponse<Void> apiResponse = ApiResponse.error(
            String.valueOf(statusCode.value()),
            ex.getMessage()
        );

        return super.handleExceptionInternal(ex, apiResponse, headers, statusCode, request);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(NotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateException(
        DuplicateException ex
    ) {
        log.warn("Duplicate resource detected: {}", ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiResponse.error("DUPLICATE", ex.getMessage()));
    }

    @ExceptionHandler(InvalidValueException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidValueException(InvalidValueException ex) {
        log.warn("Invalid value: {}", ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(
                "INVALID_VALUE", ex.getMessage())
            );
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPasswordException(
        InvalidPasswordException ex) {
        log.warn("Invalid password: {}", ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(
                "INVALID_PASSWORD", ex.getMessage()
            ));
    }

    @ExceptionHandler(AccountDeactivatedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountDeactivatedException(
        AccountDeactivatedException ex) {
        log.warn("Authentication failed : {}", ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(
                "ACCOUNT_DEACTIVATED", ex.getMessage()
            ));
    }

    @ExceptionHandler(IdempotencyException.class)
    public ResponseEntity<ApiResponse<Void>> handleIdempotencyException(IdempotencyException ex) {
        log.warn("Idempotency issue: {}", ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiResponse.error("PROCESS_IN_PROGRESS", ex.getMessage()));
    }

    @ExceptionHandler(FileException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileException(FileException ex) {
        log.warn("File exception occurred: {}", ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("FILE_EXCEPTION", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("Unhandled exception occurred: ", ex);

        String message = (ex.getMessage() != null && !ex.getMessage().isBlank())
            ? ex.getMessage()
            : "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_SERVER_ERROR", message));
    }
}
