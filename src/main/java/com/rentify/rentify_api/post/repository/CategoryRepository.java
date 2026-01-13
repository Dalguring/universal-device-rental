package com.rentify.rentify_api.post.repository;

import com.rentify.rentify_api.post.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
