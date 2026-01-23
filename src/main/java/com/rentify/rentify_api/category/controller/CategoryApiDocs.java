package com.rentify.rentify_api.category.controller;

import com.rentify.rentify_api.category.dto.CategoryResponse;
import com.rentify.rentify_api.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Category API", description = "카테고리 조회 API")
public interface CategoryApiDocs {

    @Operation(summary = "카테고리 조회", description = "전체 카테고리 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "카테고리 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "success": true,
                            "code": "SUCCESS",
                            "message": "요청이 성공했습니다.",
                            "data": {
                                "categories": [
                                    {
                                        "id": 1,
                                        "name": "갤럭시 울트라"
                                    },
                                    {
                                        "id": 2,
                                        "name": "아이폰"
                                    }
                                ]
                            }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "success": false,
                            "code": "INTERNAL_SERVER_ERROR",
                            "message": "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                            "data": null
                        }
                        """
                )
            )
        )
    })
    @GetMapping
    ResponseEntity<ApiResponse<CategoryResponse>> getCategory();
}
