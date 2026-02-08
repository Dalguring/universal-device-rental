package com.rentify.rentify_api.category.controller;

import com.rentify.rentify_api.category.dto.CategoryInfo;
import com.rentify.rentify_api.category.dto.CategoryResponse;
import com.rentify.rentify_api.category.service.CategoryService;
import com.rentify.rentify_api.common.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController implements CategoryApiDocs {

    private final CategoryService categoryService;

    @Override
    @GetMapping
    // TODO: cache 처리
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory() {
        List<CategoryInfo> categoryInfos = categoryService.getCategory();
        return ResponseEntity.ok(
            ApiResponse.success(HttpStatus.OK, new CategoryResponse(categoryInfos))
        );
    }
}
