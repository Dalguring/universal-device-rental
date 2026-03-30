package com.rentify.rentify_api.category.service;

import com.rentify.rentify_api.category.dto.CategoryInfo;
import com.rentify.rentify_api.category.entity.Category;
import com.rentify.rentify_api.category.repository.CategoryRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Cacheable(value = "categories")
    @Transactional(readOnly = true)
    public List<CategoryInfo> getCategory() {
        List<Category> categories = categoryRepository.findAll();


        return categories.stream()
            .map(category -> CategoryInfo.builder()
                .id(category.getId())
                .name(category.getName())
                .build())
            .collect(Collectors.toList());
    }
}
