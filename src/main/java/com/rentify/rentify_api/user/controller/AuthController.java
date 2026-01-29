package com.rentify.rentify_api.user.controller;


import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.user.dto.AuthMeResponse;
import com.rentify.rentify_api.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController implements AuthApiDocs {

    private final AuthService authService;

    @GetMapping("/me")
    @Override
    public ResponseEntity<ApiResponse<AuthMeResponse>> me(@AuthenticationPrincipal Long userId) {
        AuthMeResponse response = authService.getMe(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
