package com.rentify.rentify_api.post.dto;

import com.rentify.rentify_api.post.entity.PostStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostFormRequest {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 50, message = "Title must be 50 characters or less")
    private String title;

    @Size(max = 5000, message = "Description must be 5000 characters or less")
    private String description;

    @NotNull(message = "Price per day is required")
    @Min(value = 0, message = "Price per day must be 0 or greater")
    private Integer pricePerDay;

    @NotNull(message = "Max rental days is required")
    @Min(value = 1, message = "Max rental days must be at least 1")
    private Integer maxRentalDays;

    @NotNull(message = "Parcel available is required")
    private Boolean isParcel;

    @NotNull(message = "Meetup available is required")
    private Boolean isMeetup;

    private PostStatus status;

    @NotEmpty
    private List<@Pattern(
        regexp = "^http://[^/]+/images/.*",
        message = "Invalid image URL format"
    ) String> imageUrls;
}
