package com.rentify.rentify_api.rental.dto;

import com.rentify.rentify_api.rental.entity.ReceiveMethod;
import com.rentify.rentify_api.rental.entity.Rental;
import com.rentify.rentify_api.rental.entity.RentalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalResponse {

    private Long rentalId;
    private Long userId;
    private Long postId;
    private String lenderName; // 게시글작성자
  //  private String borrowerName; // 대여자
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private ReceiveMethod receiveMethod;
    private RentalStatus status;
    private Integer totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RentalResponse from(Rental rental) {
        return RentalResponse.builder()
                .rentalId(rental.getId())
                .userId(rental.getUser().getId())
                //.borrowerName(rental.getUser().getName())
                .lenderName(rental.getPost().getUser().getName())
                .postId(rental.getPost().getId())
                .title(rental.getPost().getTitle())     // Post 제목
                .startDate(rental.getStartDate())
                .endDate(rental.getEndDate())
                .receiveMethod(rental.getReceiveMethod())
                .status(rental.getStatus())
                .totalPrice(rental.getTotalPrice())
                .createdAt(rental.getCreatedAt())
                .updatedAt(rental.getUpdatedAt())
                .build();
    }
}
