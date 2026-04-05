package com.rentify.rentify_api.point.dto;

import com.rentify.rentify_api.point.entity.PointHistory;
import com.rentify.rentify_api.point.entity.PointHistoryType;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record PointHistoryResponse(
    Long historyId,
    PointHistoryType type,
    Integer amount,
    Integer finalBalance,
    String description,
    LocalDateTime createdAt
) {
    public static PointHistoryResponse from(PointHistory pointHistory) {
        return PointHistoryResponse.builder()
            .historyId(pointHistory.getId())
            .type(pointHistory.getType())
            .amount(pointHistory.getAmount())
            .finalBalance(pointHistory.getFinalBalance())
            .description(pointHistory.getDescription())
            .createdAt(pointHistory.getCreateAt())
            .build();
    }
}
