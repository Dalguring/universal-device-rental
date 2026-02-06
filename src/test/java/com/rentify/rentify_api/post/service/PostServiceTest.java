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
import com.rentify.rentify_api.common.exception.InvalidValueException;
import com.rentify.rentify_api.common.exception.NotFoundException;
import com.rentify.rentify_api.common.idempotency.IdempotencyKey;
import com.rentify.rentify_api.common.idempotency.IdempotencyKeyRepository;
import com.rentify.rentify_api.common.idempotency.IdempotencyStatus;
import com.rentify.rentify_api.image.entity.Image;
import com.rentify.rentify_api.image.service.ImageService;
import com.rentify.rentify_api.post.dto.PostDetailResponse;
import com.rentify.rentify_api.post.dto.PostFormRequest;
import com.rentify.rentify_api.post.entity.Post;
import com.rentify.rentify_api.post.repository.PostHistoryRepository;
import com.rentify.rentify_api.post.repository.PostRepository;
import com.rentify.rentify_api.user.entity.User;
import com.rentify.rentify_api.user.exception.UserNotFoundException;
import com.rentify.rentify_api.user.repository.UserRepository;
import java.util.ArrayList;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;

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
    private PostFormRequest request;

    @BeforeEach
    void setUp() {
        idempotencyKey = UUID.randomUUID();
        userId = 1L;
        request = new PostFormRequest();
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

    @Test
    @DisplayName("게시글 상세 조회 성공")
    void get_post_success() {
        // given
        Long postId = 1L;

        User mockUser = User.builder()
            .id(100L)
            .name("테스트유저")
            .build();

        Category mockCategory = Category.builder()
            .id(200L)
            .name("전자기기")
            .build();

        Image mockImage1 = Image.builder().url("http://test.com/1.jpg").build();
        Image mockImage2 = Image.builder().url("http://test.com/2.jpg").build();

        Post mockPost = Post.builder()
            .id(postId)
            .title("테스트 제목")
            .description("테스트 내용")
            .pricePerDay(5000)
            .maxRentalDays(5)
            .isParcel(true)
            .images(List.of(mockImage1, mockImage2))
            .user(mockUser)
            .category(mockCategory)
            .build();

        given(postRepository.findById(postId)).willReturn(Optional.of(mockPost));

        // when
        PostDetailResponse response = postService.getPost(postId);

        // then
        assertThat(response.getPostId()).isEqualTo(postId);
        assertThat(response.getTitle()).isEqualTo("테스트 제목");
        assertThat(response.getUserId()).isEqualTo(100L);
        assertThat(response.getUserName()).isEqualTo("테스트유저");
        assertThat(response.getCategoryName()).isEqualTo("전자기기");
        assertThat(response.getImageUrls()).hasSize(2);
        assertThat(response.getImageUrls()).containsExactly(
            "http://test.com/1.jpg",
            "http://test.com/2.jpg"
        );

        // verify
        verify(postRepository, times(1)).findById(any());
    }

    @Test
    @DisplayName("게시글 상세 조회 실패")
    void get_post_notfound() {
        // given
        Long invalidPostId = 10L;

        given(postRepository.findById(invalidPostId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getPost(invalidPostId))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("게시글을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void update_post_success() {
        // given
        Long postId = 1L;

        User mockUser = User.builder()
            .id(userId)
            .build();

        Post mockPost = Post.builder()
            .id(postId)
            .title("테스트 제목")
            .description("테스트 내용")
            .maxRentalDays(90)
            .user(mockUser)
            .isParcel(true)
            .isMeetup(false)
            .thumbnailUrl("http://old-image.com/old.jpg")
            .images(new ArrayList<>())
            .build();

        Category mockCategory = Category.builder()
            .id(request.getCategoryId())
            .name("테스트 카테고리")
            .build();

        request.setTitle("수정 테스트 제목");
        request.setDescription("수정 테스트 내용");
        request.setPricePerDay(5000);
        request.setMaxRentalDays(90);
        request.setIsParcel(true);
        request.setIsMeetup(false);
        request.setImageUrls(List.of("http://new-image.com/new.jpg"));

        given(postRepository.findById(postId)).willReturn(Optional.of(mockPost));
        given(categoryRepository.findById(request.getCategoryId()))
            .willReturn(Optional.of(mockCategory));

        // when
        postService.updatePost(postId, userId, request);

        // then
        assertThat(mockPost.getTitle()).isEqualTo("수정 테스트 제목");
        assertThat(mockPost.getDescription()).isEqualTo("수정 테스트 내용");
        assertThat(mockPost.getMaxRentalDays()).isEqualTo(90);
        assertThat(mockPost.getThumbnailUrl()).isEqualTo("http://new-image.com/new.jpg");

        // verify
        verify(imageService, times(1)).saveImages(any(), any());
        verify(postHistoryRepository, times(1)).save(any());
        verify(postRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 게시글 수정 요청")
    void not_found_post() {
        // given
        Long invalidPostId = 10L;

        given(postRepository.findById(invalidPostId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.updatePost(invalidPostId, userId, request))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("권한이 없는 사용자가 게시글 수정 요청")
    void unauthorized_update_post_request() {
        // given
        Long postId = 1L;

        User mockUser = User.builder()
            .id(10L)
            .build();

        Post mockPost = Post.builder()
            .id(postId)
            .user(mockUser)
            .build();

        given(postRepository.findById(postId)).willReturn(Optional.of(mockPost));

        // when & then
        assertThatThrownBy(() -> postService.updatePost(postId, userId, request))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("수정 권한이 없습니다.");
    }

    @Test
    @DisplayName("게시글 전체 조회 성공")
    void get_posts_success() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createAt").descending());

        Post mockPost = Post.builder()
            .id(1L)
            .title("검색된 게시글")
            .description("내용")
            .user(User.builder().id(1L).name("테스트유저").build())
            .category(Category.builder().id(1L).name("테스트카테고리").build())
            .images(new ArrayList<>())
            .build();

        Page<Post> mockPage = new PageImpl<>(List.of(mockPost), pageable, 1);

        given(postRepository.findAllSearch(null, null, null, pageable)).willReturn(mockPage);

        // when
        Page<PostDetailResponse> result = postService.getPosts(null, null, null, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("검색된 게시글");
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 전달 시 게시글 조회 실패")
    void get_posts_failed_by_category() {
        // given
        Long categoryId = 100L;

        given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getPosts(categoryId, null, null, Pageable.unpaged()))
            .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 상태값 전달 시 게시글 조회 실패")
    void get_posts_failed_by_status() {
        // given
        String status = "NOT_FOUND_STATUS";

        // when & then
        assertThatThrownBy(() -> postService.getPosts(null, status, null, Pageable.unpaged()))
            .isInstanceOf(InvalidValueException.class);
    }

    @Test
    @DisplayName("존재하지 않는 정렬순서 전달 시 게시글 조회 실패")
    void get_posts_failed_by_page_sort() {
        // given
        Pageable pageable = PageRequest.of(0, 5, Sort.by("not_exists_sort").descending());

        // when & then
        assertThatThrownBy(() -> postService.getPosts(null, null, null, pageable))
            .isInstanceOf(InvalidValueException.class)
            .hasMessageContaining("정렬 기준 'not_exists_sort'은(는) 지원하지 않습니다");
    }

    @Test
    @DisplayName("존재하지 않는 키워드 전달 시 게시글 조회 내용 없음")
    void get_posts_none_by_keyword() {
        // given
        Pageable pageable = Pageable.unpaged();
        String keyword = "not_exist_keyword";

        given(postRepository.findAllSearch(null, null, keyword, pageable)).willReturn(Page.empty());

        // when
        Page<PostDetailResponse> result = postService.getPosts(null, null, keyword, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }
}
