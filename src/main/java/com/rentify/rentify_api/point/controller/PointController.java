package com.rentify.rentify_api.point.controller;

import com.rentify.rentify_api.point.dto.PointHistoryResponse;
import com.rentify.rentify_api.point.service.PointService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController implements PointApiDocs {

    private final PointService pointService;

    @Override
    @GetMapping("/history")
    public ResponseEntity<List<PointHistoryResponse>> getPointHistory(
        @AuthenticationPrincipal Long userId
    ) {
        List<PointHistoryResponse> histories = pointService.getPointHistories(userId);
        return ResponseEntity.ok(histories);
    }
}
