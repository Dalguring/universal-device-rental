package com.rentify.rentify_api.rental.dto;

import com.rentify.rentify_api.rental.entity.ReceiveMethod;
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
    private LocalDate startDate;
    private LocalDate endDate;
    private ReceiveMethod receiveMethod;
    private RentalStatus status;
    private Integer totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
