package com.rentify.rentify_api.post.dto;

import com.rentify.rentify_api.image.entity.Image;
import com.rentify.rentify_api.post.entity.Post;
import com.rentify.rentify_api.post.entity.PostStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.rentify.rentify_api.rental.entity.Rental;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
    private List<RentalPeriod> rentalPeriods;

    public static PostDetailResponse from(Post post, List<Rental> rentals) {
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
                .rentalPeriods(rentals.stream()
                        .map(RentalPeriod::from)
                        .collect(Collectors.toList()))
                .build();
    }

    // 기존 from 메서드 오버로드 (rental이 없는 경우 빈 리스트 사용)
    public static PostDetailResponse from(Post post) {
        return from(post, List.of());
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RentalPeriod {
        private Long rentalId;
        private LocalDate startDate;
        private LocalDate endDate;

        public static RentalPeriod from(Rental rental) {
            return RentalPeriod.builder()
                    .rentalId(rental.getId())
                    .startDate(rental.getStartDate())
                    .endDate(rental.getEndDate())
                    .build();
        }
    }
}
