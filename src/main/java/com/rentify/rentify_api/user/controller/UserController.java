package com.rentify.rentify_api.user.controller;

import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.user.dto.CreateUserRequest;
import com.rentify.rentify_api.user.dto.UserResponse;
import com.rentify.rentify_api.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "사용자 관리 API", description = "사용자 관련 API 설정 및 관리")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createUser(@Valid @RequestBody CreateUserRequest request) {
        Long userId = userService.signup(request);
        URI location = URI.create("/api/users/" + userId);

        return ResponseEntity.created(location).body(ApiResponse.success(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserInfo(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
