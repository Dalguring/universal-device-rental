package com.rentify.rentify_api.post.service;

import com.rentify.rentify_api.category.entity.Category;
import com.rentify.rentify_api.category.exception.CategoryNotFoundException;
import com.rentify.rentify_api.category.repository.CategoryRepository;
import com.rentify.rentify_api.common.exception.IdempotencyException;
import com.rentify.rentify_api.common.idempotency.IdempotencyKey;
import com.rentify.rentify_api.common.idempotency.IdempotencyKeyRepository;
import com.rentify.rentify_api.common.idempotency.IdempotencyStatus;
import com.rentify.rentify_api.post.dto.CreatePostRequest;
import com.rentify.rentify_api.post.entity.Post;
import com.rentify.rentify_api.post.entity.PostHistory;
import com.rentify.rentify_api.post.repository.PostHistoryRepository;
import com.rentify.rentify_api.post.repository.PostRepository;
import com.rentify.rentify_api.user.entity.User;
import com.rentify.rentify_api.user.exception.UserNotFoundException;
import com.rentify.rentify_api.user.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PostHistoryRepository postHistoryRepository;

    @Transactional
    public Long createPost(UUID idempotencyKey, Long userId, CreatePostRequest request) {
        Optional<IdempotencyKey> existKey = idempotencyKeyRepository.findById(idempotencyKey);

        if (existKey.isPresent()) {
            IdempotencyKey key = existKey.get();

            if (key.getStatus() == IdempotencyStatus.SUCCESS) {
                Map<String, Object> responseBody = key.getResponseBody();
                return ((Number)responseBody.get("postId")).longValue();
            }

            throw new IdempotencyException("이전 게시글이 생성 중 입니다. 잠시 후 결과를 확인해주세요.");
        }

        IdempotencyKey key = IdempotencyKey.builder()
            .idempotencyKey(idempotencyKey)
            .domain("POST")
            .status(IdempotencyStatus.PENDING)
            .build();

        try {
            key = idempotencyKeyRepository.saveAndFlush(key);
        } catch (DataIntegrityViolationException e) {
            throw new IdempotencyException("이전 게시글이 생성 중 입니다. 잠시 후 결과를 확인해주세요.");
        }

        try {
            User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(CategoryNotFoundException::new);

            Post post = Post.builder()
                .user(user)
                .category(category)
                .title(request.getTitle())
                .description(request.getDescription())
                .pricePerDay(request.getPricePerDay())
                .maxRentalDays(request.getMaxRentalDays())
                .isParcel(request.getIsParcel())
                .isMeetup(request.getIsMeetup())
                .thumbnailUrl(request.getThumbnailUrl())
                .build();

            Post savedPost = postRepository.save(post);
            postHistoryRepository.save(
                PostHistory.builder()
                    .postId(savedPost.getId())
                    .afterValue(savedPost.toJson())
                    .build()
            );
            // TODO: image 테이블 update (썸네일 URL to ID로 전환 고려)
            Map<String, Object> successData = new HashMap<>();
            successData.put("postId", savedPost.getId());

            key.success(201, successData);

            return savedPost.getId();
        } catch (Exception e) {
            key.fail();
            throw e;
        }
    }
}
