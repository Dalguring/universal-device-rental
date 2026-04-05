package com.rentify.rentify_api.point.controller;

import com.rentify.rentify_api.point.dto.PointHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Point API", description = "포인트 관련 API")
public interface PointApiDocs {

    @Operation(
        summary = "포인트 이력 조회",
        description = "로그인한 사용자의 포인트 이력을 전체 조회합니다. 최신순으로 정렬됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "포인트 이력 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class),
                examples = @ExampleObject(
                    value = """
                        [
                          {
                            "historyId": 1,
                            "type": "EARN",
                            "amount": 1000,
                            "finalBalance": 5000,
                            "description": "대여 적립",
                            "createdAt": "2026-04-05T10:30:00"
                          },
                          {
                            "historyId": 2,
                            "type": "SPEND",
                            "amount": -500,
                            "finalBalance": 4000,
                            "description": "쿠폰 사용",
                            "createdAt": "2026-04-04T15:20:00"
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패 (로그인 필요)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": false, \"code\": \"401\", \"message\": \"인증이 필요합니다.\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 사용자",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": false, \"code\": \"404\", \"message\": \"존재하지 않는 사용자입니다.\", \"data\": null}"
                )
            )
        )
    })
    @GetMapping("/history")
    ResponseEntity<List<PointHistoryResponse>> getPointHistory(
        @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );
}
