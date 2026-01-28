package com.rentify.rentify_api.user.service;

import com.rentify.rentify_api.user.dto.AuthMeResponse;
import com.rentify.rentify_api.user.entity.User;
import com.rentify.rentify_api.user.exception.UnauthenticatedException;
import com.rentify.rentify_api.user.exception.UserNotFoundException;
import com.rentify.rentify_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;

//    public AuthMeResponse getMe() {
//        Long userId = getCurrentUserId();
//
//        User user = userRepository.findById(userId)
//            .orElseThrow(UserNotFoundException::new);
//
//        return AuthMeResponse.from(user);
//    }
//
//    private Long getCurrentUserId() {
//        Authentication authentication =
//            SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new UnauthenticatedException();
//        }
//
//        return (Long) authentication.getPrincipal();
//    }
    public AuthMeResponse getMe(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return AuthMeResponse.from(user);
    }
}