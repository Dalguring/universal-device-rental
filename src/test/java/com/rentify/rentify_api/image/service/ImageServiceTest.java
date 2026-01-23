package com.rentify.rentify_api.image.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import com.rentify.rentify_api.image.entity.Image;
import com.rentify.rentify_api.image.exception.FileLimitExceededException;
import com.rentify.rentify_api.image.exception.FileSizeExceededException;
import com.rentify.rentify_api.image.exception.FileTypeNotAllowedException;
import com.rentify.rentify_api.image.repository.ImageRepository;
import com.rentify.rentify_api.post.entity.Post;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private ImageService imageService;

    @Test
    @DisplayName("이미지 업로드 성공")
    void upload_success(@TempDir Path tempDir) {
        // given
        MockMultipartFile file1 = new MockMultipartFile(
            "files",
            "test1.jpg",
            "image/jpeg",
            "test image1".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
            "files",
            "test2.png",
            "image/png",
            "test image2".getBytes()
        );

        List<MultipartFile> files = List.of(file1, file2);

        ReflectionTestUtils.setField(imageService, "uploadPath", tempDir.toString());
        ReflectionTestUtils.setField(imageService, "baseUrl", "http://test.com");

        // when
        List<String> results = imageService.uploadImages(files);

        // then
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(url -> url.startsWith("http://test.com/images/"));
        assertThat(results).allMatch(url -> url.endsWith(".jpg") || url.endsWith(".png"));
    }

    @Test
    @DisplayName("파일 개수 초과 오류")
    void file_count_limit_exceed_error() {
        // given
        List<MultipartFile> files = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            files.add(
                new MockMultipartFile(
                    "files",
                    "testfile",
                    "image/png",
                    "test file content".getBytes()
                )
            );
        }

        ReflectionTestUtils.setField(imageService, "uploadPath", "test/path");

        // when & then
        assertThatThrownBy(() -> imageService.uploadImages(files))
            .isInstanceOf(FileLimitExceededException.class)
            .hasMessage("파일은 최대 5개까지만 업로드 가능합니다.");
    }

    @Test
    @DisplayName("파일 크기 초과 오류")
    void file_size_limit_exceed_error() {
        // given
        byte[] content = new byte[10 * 1024 * 1024 + 1];

        MockMultipartFile file = new MockMultipartFile(
            "files",
            "large.jpg",
            "image/jpeg",
            content
        );

        List<MultipartFile> files = List.of(file);

        // when & then
        assertThatThrownBy(() -> imageService.uploadImages(files))
            .isInstanceOf(FileSizeExceededException.class)
            .hasMessage("최대 10MB 까지만 업로드 가능합니다.");
    }

    @Test
    @DisplayName("지원하지 않는 파일 형식 오류")
    void not_supported_file_error() {
        // given
        MockMultipartFile file = new MockMultipartFile(
            "files",
            "pdf_file",
            "application/pdf",
            "pdf content".getBytes()
        );

        List<MultipartFile> files = List.of(file);

        // when & then
        assertThatThrownBy(() -> imageService.uploadImages(files))
            .isInstanceOf(FileTypeNotAllowedException.class)
            .hasMessage("지원하지 않는 파일 형식입니다.");
    }

    @Test
    @DisplayName("이미지 저장 성공")
    void save_image_success() {
        // given
        Post post = Post.builder()
            .id(1L)
            .title("테스트 게시글")
            .build();

        List<String> imageUrls = List.of(
            "http://test.com:8080/images/test1.png",
            "http://test.com:8080/images/test2.jpg"
        );

        // when
        imageService.saveImages(post, imageUrls);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Image>> captor = ArgumentCaptor.forClass(List.class);

        verify(imageRepository, times(1)).saveAll(captor.capture());

        List<Image> savedImages = captor.getValue();
        assertThat(savedImages).hasSize(2);

        Image file1 = savedImages.get(0);
        assertThat(file1.getUrl()).isEqualTo("http://test.com:8080/images/test1.png");
        assertThat(file1.getFilename()).isEqualTo("test1.png");
        assertThat(file1.getOrder()).isEqualTo((short) 0);
        assertThat(file1.getPost()).isEqualTo(post);

        Image file2 = savedImages.get(1);
        assertThat(file2.getUrl()).isEqualTo("http://test.com:8080/images/test2.jpg");
        assertThat(file2.getOrder()).isEqualTo((short) 1);
    }
}