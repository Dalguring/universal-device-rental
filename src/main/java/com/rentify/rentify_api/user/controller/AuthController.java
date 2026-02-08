package com.rentify.rentify_api.user.controller;


import com.rentify.rentify_api.common.exception.InvalidValueException;
import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.user.dto.AuthMeResponse;
import com.rentify.rentify_api.user.service.AuthService;
import com.rentify.rentify_api.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController implements AuthApiDocs {

    private final AuthService authService;
    private final UserService userService;

    @GetMapping("/me")
    @Override
    public ResponseEntity<ApiResponse<AuthMeResponse>> me(@AuthenticationPrincipal Long userId) {
        AuthMeResponse response = authService.getMe(userId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, response));
    }

    @PostMapping("/refresh")
    @Override
    public ResponseEntity<ApiResponse<Void>> refreshToken(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        // 쿠키에서 RefreshToken 가져오기
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            throw new InvalidValueException("RefreshToken이 없습니다.");
        }

        // DB에서 RefreshToken 검증 후 새로운 AccessToken 발급
        String newAccessToken = userService.refreshAccessToken(refreshToken);

        // 새로운 AccessToken을 쿠키에 저장
        Cookie accessTokenCookie = new Cookie("accessToken", newAccessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(30 * 60);  // 30분
        response.addCookie(accessTokenCookie);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "토큰 갱신 성공"));
    }
}
