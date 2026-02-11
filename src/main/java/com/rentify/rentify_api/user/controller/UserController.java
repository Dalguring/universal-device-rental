package com.rentify.rentify_api.user.controller;

import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.post.dto.PostDetailResponse;
import com.rentify.rentify_api.user.dto.CreateUserRequest;
import com.rentify.rentify_api.user.dto.LoginRequest;
import com.rentify.rentify_api.user.dto.UserResponse;
import com.rentify.rentify_api.user.entity.LoginResponse;
import com.rentify.rentify_api.user.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

        return ResponseEntity.created(location)
            .body(ApiResponse.success(HttpStatus.CREATED, "회원가입 성공"));
    }

    @Hidden
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserInfo(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, response));
    }

    @PostMapping("/login")
    @Override
    public ResponseEntity<ApiResponse<Void>> login(
        @RequestBody LoginRequest request,
        HttpServletResponse httpResponse
    ) {
        LoginResponse response = userService.login(request);

        // AccessToken 쿠키 설정
        Cookie accessTokenCookie = new Cookie("accessToken", response.getAccessToken());
        accessTokenCookie.setHttpOnly(true);    // XSS 공격 방지
        //accessTokenCookie.setSecure(true);    // HTTPS에서만 전송
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(30 * 60);  // 30분
        httpResponse.addCookie(accessTokenCookie);

        // RefreshToken 쿠키 설정
        Cookie refreshTokenCookie = new Cookie("refreshToken", response.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        //refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(14 * 24 * 60 * 60);  // 14일
        httpResponse.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "로그인 성공"));
    }

    @PostMapping("/logout")
    @Override
    public ResponseEntity<ApiResponse<Void>> logout(
        @AuthenticationPrincipal Long userId,
        HttpServletResponse response
    ) {
        // RefreshToken DB에서 삭제
        userService.logout(userId);

        // AccessToken 쿠키 삭제
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);  // 즉시 만료
        response.addCookie(accessTokenCookie);

        // RefreshToken 쿠키 삭제
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);  // 즉시 만료
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "로그아웃 성공"));
    }

    @Override
    @GetMapping("/me/posts")
    public ResponseEntity<ApiResponse<Page<PostDetailResponse>>> getMyPosts(
        @AuthenticationPrincipal Long userId,
        @RequestParam(defaultValue = "false") boolean includeHidden,
        @ParameterObject @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostDetailResponse> posts = userService.getMyPosts(userId, includeHidden, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, posts));
    }

}
