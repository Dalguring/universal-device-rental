package com.rentify.rentify_api.category.repository;

import com.rentify.rentify_api.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
