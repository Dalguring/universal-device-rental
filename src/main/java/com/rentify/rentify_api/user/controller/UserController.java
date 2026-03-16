package com.rentify.rentify_api.user.controller;

import com.rentify.rentify_api.common.idempotency.Idempotent;
import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.common.util.CookieUtil;
import com.rentify.rentify_api.post.dto.PostDetailResponse;
import com.rentify.rentify_api.rental.dto.RentalResponse;
import com.rentify.rentify_api.user.dto.CreateUserRequest;
import com.rentify.rentify_api.user.dto.GoogleLoginRequest;
import com.rentify.rentify_api.user.dto.LoginRequest;
import com.rentify.rentify_api.user.dto.PasswordUpdateRequest;
import com.rentify.rentify_api.user.dto.UserUpdateRequest;
import com.rentify.rentify_api.user.entity.LoginResponse;
import com.rentify.rentify_api.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.net.URI;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApiDocs {

    private final UserService userService;

    private static final int ACCESS_TOKEN_MAX_AGE = 30 * 60;
    private static final int REFRESH_TOKEN_MAX_AGE = 14 * 24 * 60 * 60;

    @Override
    @PostMapping
    @Idempotent
    public ResponseEntity<ApiResponse<Void>> createUser(
        @Valid @RequestBody CreateUserRequest request
    ) {
        Long userId = userService.signup(request);
        URI location = URI.create("/api/users/" + userId);

        return ResponseEntity.created(location)
            .body(ApiResponse.success(HttpStatus.CREATED, "회원가입 성공"));
    }

    @PostMapping("/login")
    @Override
    public ResponseEntity<ApiResponse<Void>> login(
        @RequestBody LoginRequest request,
        HttpServletResponse httpResponse
    ) {
        LoginResponse response = userService.login(request);

        CookieUtil.addTokenCookie(
            httpResponse, "accessToken", response.getAccessToken(), ACCESS_TOKEN_MAX_AGE
        );
        CookieUtil.addTokenCookie(
            httpResponse, "refreshToken", response.getAccessToken(), REFRESH_TOKEN_MAX_AGE
        );

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "로그인 성공"));
    }

    @PostMapping("/login/google")
    @Override
    public ResponseEntity<ApiResponse<Void>> googleLogin(
        @Valid @RequestBody GoogleLoginRequest request,
        HttpServletResponse httpResponse
    ) {
        LoginResponse response = userService.oauthLogin(request.idToken());

        CookieUtil.addTokenCookie(
            httpResponse, "accessToken", response.getAccessToken(), ACCESS_TOKEN_MAX_AGE
        );
        CookieUtil.addTokenCookie(
            httpResponse, "refreshToken", response.getRefreshToken(), REFRESH_TOKEN_MAX_AGE
        );

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "구글 로그인 성공"));
    }

    @PostMapping("/logout")
    @Override
    public ResponseEntity<ApiResponse<Void>> logout(
        @AuthenticationPrincipal Long userId,
        HttpServletResponse response
    ) {
        userService.logout(userId);

        CookieUtil.deleteCookie(response, "accessToken");
        CookieUtil.deleteCookie(response, "refreshToken");

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

    @Override
    @GetMapping("/me/rentals")
    public ResponseEntity<ApiResponse<Page<RentalResponse>>> getMyRentals(
        @AuthenticationPrincipal Long userId,
        @RequestParam(required = false) String role,
        @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<RentalResponse> rentals = userService.getMyRentals(userId, role, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, rentals));
    }

    @Override
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody PasswordUpdateRequest request
    ) {
        userService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "패스워드 변경 성공"));
    }

    @Override
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateUserInfo(
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody UserUpdateRequest request
    ) {
        userService.updateUserinfo(userId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "회원정보 수정 성공"));
    }
}
