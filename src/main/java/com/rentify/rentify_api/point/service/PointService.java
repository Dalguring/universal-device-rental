package com.rentify.rentify_api.point.service;

import com.rentify.rentify_api.point.dto.PointHistoryResponse;
import com.rentify.rentify_api.point.repository.PointHistoryRepository;
import com.rentify.rentify_api.user.entity.User;
import com.rentify.rentify_api.user.exception.UserNotFoundException;
import com.rentify.rentify_api.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final PointHistoryRepository pointHistoryRepository;
    private final UserRepository userRepository;

    public List<PointHistoryResponse> getPointHistories(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(UserNotFoundException::new);

        return pointHistoryRepository.findAllByUserOrderByCreateAtDesc(user)
            .stream()
            .map(PointHistoryResponse::from)
            .toList();
    }
}
