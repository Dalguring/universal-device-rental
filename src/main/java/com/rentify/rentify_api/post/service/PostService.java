package com.rentify.rentify_api.post.service;

import com.rentify.rentify_api.category.entity.Category;
import com.rentify.rentify_api.category.exception.CategoryNotFoundException;
import com.rentify.rentify_api.category.repository.CategoryRepository;
import com.rentify.rentify_api.common.exception.IdempotencyException;
import com.rentify.rentify_api.common.exception.InvalidValueException;
import com.rentify.rentify_api.common.exception.NotFoundException;
import com.rentify.rentify_api.common.idempotency.IdempotencyKey;
import com.rentify.rentify_api.common.idempotency.IdempotencyKeyRepository;
import com.rentify.rentify_api.common.idempotency.IdempotencyStatus;
import com.rentify.rentify_api.image.service.ImageService;
import com.rentify.rentify_api.post.dto.PostDetailResponse;
import com.rentify.rentify_api.post.dto.PostFormRequest;
import com.rentify.rentify_api.post.entity.Post;
import com.rentify.rentify_api.post.entity.PostHistory;
import com.rentify.rentify_api.post.entity.PostStatus;
import com.rentify.rentify_api.post.repository.PostHistoryRepository;
import com.rentify.rentify_api.post.repository.PostRepository;
import com.rentify.rentify_api.user.entity.User;
import com.rentify.rentify_api.user.exception.UserNotFoundException;
import com.rentify.rentify_api.user.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
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
    private final ImageService imageService;

    private static final Set<String> ALLOWED_SORT_FILTERS = Set.of(
        "createAt", "pricePerDay", "title", "id"
    );

    @Transactional(readOnly = true)
    public Page<PostDetailResponse> getPosts(
        Long categoryId, String statusStr, String keyword, Pageable pageable
    ) {
        if (categoryId != null) {
            categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);
        }

        PostStatus status = null;

        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = PostStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidValueException("유효하지 않은 게시글 상태입니다.");
            }
        }

        validateSort(pageable);

        Page<Post> posts = postRepository.findAllSearch(categoryId, status, keyword, pageable);
        return posts.map(PostDetailResponse::from);
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));

        return PostDetailResponse.from(post);
    }

    @Transactional
    public Long createPost(UUID idempotencyKey, Long userId, PostFormRequest request) {
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
                .thumbnailUrl(request.getImageUrls().getFirst())
                .build();

            Post savedPost = postRepository.save(post);
            postHistoryRepository.save(
                PostHistory.builder()
                    .postId(savedPost.getId())
                    .afterValue(savedPost.toJson())
                    .build()
            );

            imageService.saveImages(savedPost, request.getImageUrls());

            Map<String, Object> successData = new HashMap<>();
            successData.put("postId", savedPost.getId());

            key.success(201, successData);

            return savedPost.getId();
        } catch (Exception e) {
            key.fail();
            throw e;
        }
    }

    @Transactional
    public Long updatePost(Long postId, Long userId, PostFormRequest request) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));

        if (!post.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(CategoryNotFoundException::new);

        String beforePost = post.toJson();

        post.update(
            category,
            request.getTitle(),
            request.getDescription(),
            request.getPricePerDay(),
            request.getMaxRentalDays(),
            request.getIsParcel(),
            request.getIsMeetup(),
            request.getStatus()
        );

        post.getImages().clear();
        imageService.saveImages(post, request.getImageUrls());

        String newThumbnail =
            request.getImageUrls().isEmpty() ? null : request.getImageUrls().getFirst();
        post.updateThumbnail(newThumbnail);

        postHistoryRepository.save(
            PostHistory.builder()
                .postId(post.getId())
                .beforeValue(beforePost)
                .afterValue(post.toJson())
                .build()
        );

        return post.getId();
    }

    private void validateSort(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            for (Sort.Order order : pageable.getSort()) {
                if (!ALLOWED_SORT_FILTERS.contains(order.getProperty())) {
                    throw new InvalidValueException(
                        String.format(
                            "정렬 기준 '%s'은(는) 지원하지 않습니다. (허용 필드: %s)",
                            order.getProperty(), ALLOWED_SORT_FILTERS
                        )
                    );
                }
            }
        }
    }
}
