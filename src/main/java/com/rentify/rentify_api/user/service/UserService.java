package com.rentify.rentify_api.user.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.rentify.rentify_api.common.exception.AccountDeactivatedException;
import com.rentify.rentify_api.common.exception.InvalidPasswordException;
import com.rentify.rentify_api.common.exception.InvalidValueException;
import com.rentify.rentify_api.common.exception.NotFoundException;
import com.rentify.rentify_api.common.jwt.JwtTokenProvider;
import com.rentify.rentify_api.post.dto.PostDetailResponse;
import com.rentify.rentify_api.post.entity.Post;
import com.rentify.rentify_api.post.repository.PostRepository;
import com.rentify.rentify_api.rental.dto.RentalResponse;
import com.rentify.rentify_api.rental.service.RentalService;
import com.rentify.rentify_api.user.dto.CreateUserRequest;
import com.rentify.rentify_api.user.dto.LoginRequest;
import com.rentify.rentify_api.user.dto.PasswordUpdateRequest;
import com.rentify.rentify_api.user.dto.UserUpdateRequest;
import com.rentify.rentify_api.user.entity.LoginResponse;
import com.rentify.rentify_api.user.entity.RefreshToken;
import com.rentify.rentify_api.user.entity.User;
import com.rentify.rentify_api.user.entity.UserRole;
import com.rentify.rentify_api.user.exception.DuplicateEmailException;
import com.rentify.rentify_api.user.exception.UnauthenticatedException;
import com.rentify.rentify_api.user.exception.UserNotFoundException;
import com.rentify.rentify_api.user.repository.RefreshTokenRepository;
import com.rentify.rentify_api.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshtokenRepository;
    private final PostRepository postRepository;
    private final RentalService rentalService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Transactional
    public Long signup(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException();
        }

        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .address(request.getAddress())
            .bank(request.getBank())
            .account(request.getAccount())
            .point(0)
            .pointVersion((short) 0)
            .role(UserRole.USER)
            .phone(request.getPhone())
            .isActive(true)
            .build();

        User savedUser = userRepository.save(user);

        return savedUser.getId();
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
        }

        return issueTokens(user);
    }

    @Transactional
    public LoginResponse oauthLogin(String idToken) {
        GoogleIdToken.Payload payload = verifyGoogleToken(idToken);

        String email = payload.getEmail();
        String name = Objects.toString(payload.get("name"), null);
        String safeName = (name != null && name.length() > 10) ? name.substring(0, 10)
            : (name != null ? name : "구글유저");

        User user = userRepository.findByEmail(email)
            .orElseGet(() -> {
                User newUser = User.builder()
                    .email(email)
                    .name(safeName)
                    .password("SOCIAL_LOGIN")
                    .build();

                return userRepository.save(newUser);
            });

        return issueTokens(user);
    }

    private Payload verifyGoogleToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

            GoogleIdToken token = verifier.verify(idToken);

            if (token != null) {
                return token.getPayload();
            } else {
                throw new UnauthenticatedException("유효하지 않은 구글 토큰입니다.");
            }
        } catch (UnauthenticatedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google Token Verification Error", e);
            throw new RuntimeException("구글 토큰 검증 중 오류가 발생했습니다.");
        }
    }

    private LoginResponse issueTokens(User user) {
        if (!user.getIsActive()) {
            throw new AccountDeactivatedException("비활성화된 계정입니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        refreshtokenRepository.save(
            RefreshToken.builder()
                .userId(user.getId())
                .token(refreshToken)
                .expiredAt(LocalDateTime.now().plusDays(14))
                .build()
        );

        return new LoginResponse(accessToken, refreshToken);
    }

    @Transactional
    public String refreshAccessToken(String refreshTokenString) {
        // DB에서 RefreshToken 조회
        RefreshToken refreshToken = refreshtokenRepository.findByToken(refreshTokenString)
            .orElseThrow(() -> new NotFoundException("RefreshToken을 찾을 수 없습니다."));

        // RefreshToken 만료 또는 무효 확인
        if (refreshToken.isExpired() || refreshToken.isRevoked()) {
            throw new NotFoundException("만료되거나 무효화된 RefreshToken입니다.");
        }

        // 유저 확인
        User user = userRepository.findById(refreshToken.getUserId())
            .orElseThrow(UserNotFoundException::new);

        if (!user.getIsActive()) {
            throw new AccountDeactivatedException("비활성화된 계정입니다.");
        }

        // [옵션] RefreshToken 만료 기간 연장 (Sliding Window 방식)
        // 사용자가 계속 사이트를 사용하는 동안에는 영구적으로 로그인 상태 유지
        // 아래 주석을 해제하면 활성화됩니다.
        // refreshToken.updateExpiredAt(LocalDateTime.now().plusDays(14));
        // refreshtokenRepository.save(refreshToken);

        // 새로운 AccessToken 발급
        return jwtTokenProvider.createAccessToken(user.getId());
    }

    @Transactional
    public void logout(Long userId) {
        // DB에서 해당 유저의 모든 RefreshToken 삭제
        refreshtokenRepository.deleteByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Page<PostDetailResponse> getMyPosts(Long userId, boolean includeHidden, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserIdWithHiddenOption(userId, includeHidden, pageable);
        return posts.map(PostDetailResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<RentalResponse> getMyRentals(Long userId, String role, Pageable pageable) {
        if (role == null) {
            // role이 없으면 전체 대여 목록
            return rentalService.getMyAllRentals(userId, pageable);
        }

        return switch (role.toLowerCase()) {
            case "borrower" -> rentalService.getMyBorrowedRentals(userId, pageable);
            case "lender" -> rentalService.getMyLendedRentals(userId, pageable);
            default -> throw new IllegalArgumentException("Invalid role parameter. Use 'borrower' or 'lender'");
        };
    }

    @Transactional
    public void changePassword(Long userId, PasswordUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("패스워드가 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new InvalidValueException("기존과 동일한 비밀번호로 변경할 수 없습니다.");
        }

        String hashedPassword = passwordEncoder.encode(request.newPassword());
        user.updatePassword(hashedPassword);
    }

    @Transactional
    public void updateUserinfo(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (request.getEmail() != null) {
            userRepository.findByEmail(request.getEmail())
                .ifPresent(u -> {
                    if (!u.getId().equals(userId)) {
                        throw new DuplicateEmailException();
                    }
                });
            user.updateEmail(request.getEmail());
        }

        if (request.getName() != null) {
            user.updateName(request.getName());
        }

        if (request.getAddress() != null) {
            user.updateAddress(request.getAddress());
        }

        if (request.getAccount() != null) {
            user.updateAccount(request.getAccount());
        }
    }
}
