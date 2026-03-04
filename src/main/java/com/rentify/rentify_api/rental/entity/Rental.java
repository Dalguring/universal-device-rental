package com.rentify.rentify_api.rental.entity;

import com.rentify.rentify_api.post.entity.Post;
import com.rentify.rentify_api.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "rentals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rental_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "actual_returned_at")
    private LocalDateTime actualReturnedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "receive_method", nullable = false)
    private ReceiveMethod receiveMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private RentalStatus status = RentalStatus.REQUESTED;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void confirm() {
        if (this.status != RentalStatus.REQUESTED) {
            throw new IllegalStateException("신청 상태에서만 확정할 수 있습니다.");
        }
        this.status = RentalStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status != RentalStatus.REQUESTED && this.status != RentalStatus.CONFIRMED) {
            throw new IllegalStateException("신청 또는 확정 상태에서만 취소할 수 있습니다.");
        }
        if (LocalDate.now().isAfter(this.startDate) || LocalDate.now().isEqual(this.startDate)) {
            throw new IllegalStateException("대여 시작일 이후에는 취소할 수 없습니다.");
        }
        this.status = RentalStatus.CANCELED;
    }
}
