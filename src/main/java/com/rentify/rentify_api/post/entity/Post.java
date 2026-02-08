package com.rentify.rentify_api.post.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rentify.rentify_api.category.entity.Category;
import com.rentify.rentify_api.image.entity.Image;
import com.rentify.rentify_api.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_per_day", nullable = false)
    private Integer pricePerDay;

    @Column(name = "max_rental_days", nullable = false)
    private Integer maxRentalDays;

    @Column(name = "is_parcel_available", nullable = false)
    private Boolean isParcel;

    @Column(name = "is_meetup_available", nullable = false)
    private Boolean isMeetup;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PostStatus status = PostStatus.AVAILABLE;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updateAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Image> images = new ArrayList<>();

    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            Map<String, Object> data = new HashMap<>();
            data.put("id", this.id);
            data.put("title", this.title);
            data.put("description", this.description);
            data.put("pricePerDay", this.pricePerDay);
            data.put("maxRentalDays", this.maxRentalDays);
            data.put("isParcel", this.isParcel);
            data.put("isMeetup", this.isMeetup);
            data.put("status", this.status);
            data.put("thumbnailUrl", this.thumbnailUrl);
            data.put("userId", this.user != null ? this.user.getId() : null);
            data.put("categoryId", this.category != null ? this.category.getId() : null);

            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }

    public void update(Category category, String title, String description,
        Integer pricePerDay, Integer maxRentalDays, Boolean isParcel, Boolean isMeetup,
        PostStatus status) {
        this.category = category;
        this.title = title;
        this.description = description;
        this.pricePerDay = pricePerDay;
        this.maxRentalDays = maxRentalDays;
        this.isParcel = isParcel;
        this.isMeetup = isMeetup;

        if (status != null) {
            this.status = status;
        }
    }

    public void updateThumbnail(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void updateStatus(PostStatus status) {
        this.status = status;
    }
}
