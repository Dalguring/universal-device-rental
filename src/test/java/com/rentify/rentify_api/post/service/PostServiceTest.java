package com.rentify.rentify_api.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.rentify.rentify_api.category.entity.Category;
import com.rentify.rentify_api.category.exception.CategoryNotFoundException;
import com.rentify.rentify_api.category.repository.CategoryRepository;
import com.rentify.rentify_api.common.exception.IdempotencyException;
import com.rentify.rentify_api.common.idempotency.IdempotencyKey;
import com.rentify.rentify_api.common.idempotency.IdempotencyKeyRepository;
import com.rentify.rentify_api.common.idempotency.IdempotencyStatus;
import com.rentify.rentify_api.image.service.ImageService;
import com.rentify.rentify_api.post.dto.CreatePostRequest;
import com.rentify.rentify_api.post.entity.Post;
import com.rentify.rentify_api.post.repository.PostHistoryRepository;
import com.rentify.rentify_api.post.repository.PostRepository;
import com.rentify.rentify_api.user.entity.User;
import com.rentify.rentify_api.user.exception.UserNotFoundException;
import com.rentify.rentify_api.user.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ImageService imageService;
    @Mock
    private PostHistoryRepository postHistoryRepository;
    @InjectMocks
    private PostService postService;

    private UUID idempotencyKey;
    private Long userId;
    private CreatePostRequest request;

    @BeforeEach
    void setUp() {
        idempotencyKey = UUID.randomUUID();
        userId = 1L;
        request = new CreatePostRequest();
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void create_post_success() {
        // given
        request.setCategoryId(1L);
        request.setTitle("테스트 제목");
        request.setDescription("테스트 내용");
        request.setPricePerDay(1000);
        request.setMaxRentalDays(10);
        request.setIsParcel(true);
        request.setIsMeetup(true);
        request.setImageUrls(
            List.of(
                "http://test.com:8080/images/test1.png",
                "http//test.com:8080/images/test2.jpg"
            )
        );

        User user = User.builder()
            .id(userId)
            .email("test@test.com")
            .name("테스트 유저")
            .build();

        Category category = Category.builder()
            .id(request.getCategoryId())
            .build();

        Post savedPost = Post.builder().id(100L).build();

        given(idempotencyKeyRepository.findById(idempotencyKey)).willReturn(Optional.empty());
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(categoryRepository.findById(request.getCategoryId())).willReturn(
            Optional.of(category));
        given(idempotencyKeyRepository.saveAndFlush(any()))
            .willAnswer(invocation -> invocation.getArgument(0));
        given(postRepository.save(any(Post.class))).willReturn(savedPost);

        // when
        Long savedPostId = postService.createPost(idempotencyKey, userId, request);

        // then
        assertThat(100L).isEqualTo(savedPostId);

        // verify
        verify(idempotencyKeyRepository, times(1)).saveAndFlush(any(IdempotencyKey.class));
        verify(imageService, times(1)).saveImages(savedPost, request.getImageUrls());
        verify(postHistoryRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("멱등성 확인: 이미 성공적으로 처리된 요청(키)이면 저장된 ID를 바로 반환한다.")
    void create_post_idempotency_success() {
        //given
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("postId", 10L);

        IdempotencyKey existKey = IdempotencyKey.builder()
            .idempotencyKey(idempotencyKey)
            .status(IdempotencyStatus.SUCCESS)
            .responseBody(responseBody)
            .build();

        given(idempotencyKeyRepository.findById(idempotencyKey)).willReturn(Optional.of(existKey));

        // when
        Long postId = postService.createPost(idempotencyKey, userId, request);

        // then
        assertThat(10L).isEqualTo(postId);

        // verify
        verify(idempotencyKeyRepository, times(0)).saveAndFlush(any());
        verify(postHistoryRepository, times(0)).save(any());
        verify(imageService, times(0)).saveImages(any(), any());
        verify(postRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("멱등성 처리 예외 발생")
    void create_post_idempotency_pending() {
        //given
        IdempotencyKey pendingKey = IdempotencyKey.builder()
            .idempotencyKey(idempotencyKey)
            .status(IdempotencyStatus.PENDING)
            .build();

        given(idempotencyKeyRepository.findById(idempotencyKey)).willReturn(
            Optional.of(pendingKey));

        // when & then
        assertThatThrownBy(() -> postService.createPost(idempotencyKey, userId, request))
            .isInstanceOf(IdempotencyException.class)
            .hasMessage("이전 게시글이 생성 중 입니다. 잠시 후 결과를 확인해주세요.");

        // verify
        verify(idempotencyKeyRepository, times(0)).saveAndFlush(any());
    }

    @Test
    @DisplayName("존재하지 않는 유저 예외")
    void create_post_user_notfound() {
        // given
        Long notFoundUserId = Long.MAX_VALUE;

        given(idempotencyKeyRepository.findById(idempotencyKey)).willReturn(Optional.empty());
        given(idempotencyKeyRepository.saveAndFlush(any())).willAnswer(
            invocation -> invocation.getArgument(0));
        given(userRepository.findById(notFoundUserId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.createPost(idempotencyKey, notFoundUserId, request))
            .isInstanceOf(UserNotFoundException.class);

        // verify
        verify(idempotencyKeyRepository, times(1)).saveAndFlush(any());
        verify(imageService, times(0)).saveImages(any(), any());
        verify(postHistoryRepository, times(0)).save(any());
        verify(postRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 오류")
    void create_post_category_notfound() {
        // given
        given(idempotencyKeyRepository.findById(idempotencyKey)).willReturn(Optional.empty());
        given(idempotencyKeyRepository.saveAndFlush(any())).willAnswer(
            invocation -> invocation.getArgument(0));
        given(userRepository.findById(userId)).willReturn(Optional.of(User.builder().build()));
        given(categoryRepository.findById(request.getCategoryId())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.createPost(idempotencyKey, userId, request))
            .isInstanceOf(CategoryNotFoundException.class)
            .hasMessage("등록되지 않은 카테고리입니다.");

        // verify
        verify(idempotencyKeyRepository, times(1)).saveAndFlush(any());
        verify(imageService, times(0)).saveImages(any(), any());
        verify(postHistoryRepository, times(0)).save(any());
        verify(postRepository, times(0)).save(any());
    }
}