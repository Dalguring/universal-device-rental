package com.rentify.rentify_api.user.service;

import com.rentify.rentify_api.common.jwt.JwtTokenProvider;
import com.rentify.rentify_api.common.exception.IdempotencyException;
import com.rentify.rentify_api.common.idempotency.IdempotencyKey;
import com.rentify.rentify_api.common.idempotency.IdempotencyKeyRepository;
import com.rentify.rentify_api.common.idempotency.IdempotencyStatus;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshtokenRepository refreshtokenRepository;

    @Transactional
    public Long signup(UUID idempotencyKey, CreateUserRequest request) {
        Optional<IdempotencyKey> existKey = idempotencyKeyRepository.findById(idempotencyKey);

        if (existKey.isPresent()) {
            IdempotencyKey key = existKey.get();

            if (key.getStatus() == IdempotencyStatus.SUCCESS) {
                Map<String, Object> responseBody = key.getResponseBody();
                return ((Number) responseBody.get("userId")).longValue();
            }

            throw new IdempotencyException("이전 요청이 아직 처리 중입니다. 잠시 후 결과를 확인해주세요.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException();
        }

        IdempotencyKey key = IdempotencyKey.builder()
                .idempotencyKey(idempotencyKey)
                .domain("USER")
                .status(IdempotencyStatus.PENDING)
                .build();

        try {
            key = idempotencyKeyRepository.saveAndFlush(key);
        } catch (DataIntegrityViolationException e) {
            throw new IdempotencyException("이전 요청이 아직 처리 중입니다. 잠시 후 결과를 확인해주세요.");
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
        Map<String, Object> successData = new HashMap<>();
        successData.put("userId", savedUser.getId());

        key.success(201, successData);

        return savedUser.getId();
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
