package com.rentify.rentify_api.post.dto;

import com.rentify.rentify_api.image.entity.Image;
import com.rentify.rentify_api.post.entity.Post;
import com.rentify.rentify_api.post.entity.PostStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostDetailResponse {

    private Long postId;
    private Long userId;
    private String categoryName;
    private String userName;
    private String title;
    private String description;
    private Integer pricePerDay;
    private Integer maxRentalDays;
    private Boolean isParcel;
    private Boolean isMeetup;
    private PostStatus status;
    private List<String> imageUrls;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    public static PostDetailResponse from(Post post) {
        return PostDetailResponse.builder()
            .postId(post.getId())
            .userId(post.getUser().getId())
            .categoryName(post.getCategory().getName())
            .userName(post.getUser().getName())
            .title(post.getTitle())
            .description(post.getDescription())
            .pricePerDay(post.getPricePerDay())
            .maxRentalDays(post.getMaxRentalDays())
            .isParcel(post.getIsParcel())
            .isMeetup(post.getIsMeetup())
            .status(post.getStatus())
            .imageUrls(post.getImages()
                .stream()
                .map(Image::getUrl)
                .collect(Collectors.toList()))
            .createAt(post.getCreateAt())
            .updateAt(post.getUpdateAt())
            .build();
    }
}
