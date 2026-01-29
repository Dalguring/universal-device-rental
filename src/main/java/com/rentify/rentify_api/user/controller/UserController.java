package com.rentify.rentify_api.user.controller;

import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.user.dto.CreateUserRequest;
import com.rentify.rentify_api.user.dto.LoginRequest;
import com.rentify.rentify_api.user.dto.UserResponse;
import com.rentify.rentify_api.user.entity.LoginResponse;
import com.rentify.rentify_api.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApiDocs {

    private final UserService userService;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createUser(
        @RequestHeader(value = "Idempotency-Key") UUID idempotencyKey,
        @Valid @RequestBody CreateUserRequest request
    ) {
        Long userId = userService.signup(idempotencyKey, request);
        URI location = URI.create("/api/users/" + userId);

        return ResponseEntity.created(location).body(ApiResponse.success("회원가입 성공"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserInfo(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/login")
    @Override
    public ResponseEntity<ApiResponse<Void>> login(@RequestBody LoginRequest request, HttpServletResponse httpResponse) {
        LoginResponse response = userService.login(request);

        // JWT 토큰 쿠키 설정
        Cookie cookie = new Cookie("accessToken", response.getAccessToken());
        cookie.setHttpOnly(true);    // XSS 공격 방지
        //cookie.setSecure(true);    // HTTPS에서만 전송
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);  // 24시간 (초 단위)
        httpResponse.addCookie(cookie);

        return ResponseEntity.ok(ApiResponse.success("로그인 성공"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success());
    }

}
