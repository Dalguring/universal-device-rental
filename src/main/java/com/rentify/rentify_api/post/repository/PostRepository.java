package com.rentify.rentify_api.post.repository;

import com.rentify.rentify_api.post.entity.Post;
import com.rentify.rentify_api.post.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(
        "SELECT p FROM Post p " +
        "WHERE (:categoryId IS NULL OR p.category.id = :categoryId) " +
        "AND (:status IS NULL OR p.status = :status) " +
        "AND (:keyword IS NULL OR p.title LIKE %:keyword% OR p.description LIKE %:keyword%)"
    )
    Page<Post> findAllSearch(
        @Param("categoryId") Long categoryId,
        @Param("status") PostStatus status,
        @Param("keyword") String keyword,
        Pageable pageable
    );
}
