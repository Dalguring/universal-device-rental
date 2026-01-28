package com.rentify.rentify_api.user.controller;


import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.user.dto.AuthMeResponse;
import com.rentify.rentify_api.user.dto.UserResponse;
import com.rentify.rentify_api.user.service.AuthService;
import com.rentify.rentify_api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController implements AuthApi {

    private final AuthService authService;
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal Long userId) {
        UserResponse response = userService.getUserInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Override
    public ResponseEntity<AuthMeResponse> me() {
        return null;
    }
}
