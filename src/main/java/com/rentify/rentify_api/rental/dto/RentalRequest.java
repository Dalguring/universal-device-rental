package com.rentify.rentify_api.rental.dto;

import com.rentify.rentify_api.rental.entity.ReceiveMethod;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RentalRequest {

    @NotNull(message = "게시글 ID는 필수입니다.")
    private Long postId;

    @NotNull(message = "대여 시작일은 필수입니다.")
    //@Size(min = 8, max = 10, message = "대여 시작일은 YYYY-MM-DD 형식이어야 합니다.")
    private LocalDate startDate;

    @NotNull(message = "대여 종료일은 필수입니다.")
    //@Size(min = 8, max = 10, message = "대여 종료일은 YYYY-MM-DD 형식이어야 합니다.")
    private LocalDate endDate;

    @NotNull(message = "수령 방법은 필수입니다.")
    private ReceiveMethod receiveMethod;
}
