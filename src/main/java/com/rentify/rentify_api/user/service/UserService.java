package com.rentify.rentify_api.user.service;

import com.rentify.rentify_api.common.jwt.JwtTokenProvider;
import com.rentify.rentify_api.user.dto.CreateUserRequest;
import com.rentify.rentify_api.user.dto.LoginRequest;
import com.rentify.rentify_api.user.dto.UserResponse;
import com.rentify.rentify_api.user.entity.LoginResponse;
import com.rentify.rentify_api.user.entity.RefreshToken;
import com.rentify.rentify_api.user.entity.User;
import com.rentify.rentify_api.user.entity.UserRole;
import com.rentify.rentify_api.user.exception.DuplicateEmailException;
import com.rentify.rentify_api.user.exception.UserNotFoundException;
import com.rentify.rentify_api.user.repository.RefreshtokenRepository;
import com.rentify.rentify_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshtokenRepository refreshtokenRepository;

    @Transactional
    public Long signup(CreateUserRequest request) {
        // 멱등성 처리 (나중에 추가)

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

        User saved = userRepository.save(user);
        return saved.getId();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserInfo(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .address(user.getAddress())
                .bank(user.getBank())
                .account(user.getAccount())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .build();
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if (!user.getIsActive()) {
            throw new IllegalStateException("비활성화된 계정입니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());

        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        refreshtokenRepository.save(
                new RefreshToken(
                        user.getId(),
                        refreshToken,
                        LocalDateTime.now().plusDays(14)
                )
        );

        return new LoginResponse(accessToken, refreshToken);
    }
}
