package com.rentify.rentify_api.payment.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PaymentRequest {

    @NotNull(message = "Rental ID is required")
    private Long rentalId;

    @Nullable
    private Long userCouponId;

    @Nullable
    @Builder.Default
    private Integer pointAmount = 0;
}
