package com.rentify.rentify_api.rental.dto;

import com.rentify.rentify_api.payment.entity.Payment;
import com.rentify.rentify_api.payment.entity.PaymentStatus;
import com.rentify.rentify_api.post.entity.PostStatus;
import com.rentify.rentify_api.rental.entity.ReceiveMethod;
import com.rentify.rentify_api.rental.entity.Rental;
import com.rentify.rentify_api.rental.entity.RentalStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RentalResponse {

    private Long rentalId;
    private Long userId;
    private Long postId;
    private Long paymentId;
    private String lenderName;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private ReceiveMethod receiveMethod;
    private PostStatus postStatus;
    private RentalStatus rentalStatus;
    private String thumbnailUrl;
    private Integer totalPrice;
    private boolean canPay;
    private boolean canCancel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RentalResponse of(Rental rental, Payment payment) {
        boolean canPay = (payment == null || payment.getStatus() == PaymentStatus.FAILED)
            && (rental.getStatus() == RentalStatus.REQUESTED);
        boolean canCancel = (payment != null && payment.getStatus() == PaymentStatus.PAID)
            && (rental.getStatus() == RentalStatus.REQUESTED || rental.getStatus() == RentalStatus.CONFIRMED);

        return RentalResponse.builder()
            .rentalId(rental.getId())
            .userId(rental.getUser().getId())
            .paymentId(payment != null ? payment.getId() : null)
            .lenderName(rental.getPost().getUser().getName())
            .postId(rental.getPost().getId())
            .title(rental.getPost().getTitle())
            .startDate(rental.getStartDate())
            .endDate(rental.getEndDate())
            .receiveMethod(rental.getReceiveMethod())
            .postStatus(rental.getPost().getStatus())
            .rentalStatus(rental.getStatus())
            .thumbnailUrl(rental.getPost().getThumbnailUrl())
            .totalPrice(rental.getTotalPrice())
            .canPay(canPay)
            .canCancel(canCancel)
            .createdAt(rental.getCreatedAt())
            .updatedAt(rental.getUpdatedAt())
            .build();
    }
}
