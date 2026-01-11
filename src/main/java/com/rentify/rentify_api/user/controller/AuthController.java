package com.rentify.rentify_api.user.controller;


import com.rentify.rentify_api.user.dto.AuthMeResponse;
import com.rentify.rentify_api.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> me() {
        return ResponseEntity.ok(authService.getMe());
    }
}
