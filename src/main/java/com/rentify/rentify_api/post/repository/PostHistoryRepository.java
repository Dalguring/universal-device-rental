package com.rentify.rentify_api.post.repository;

import com.rentify.rentify_api.post.entity.PostHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostHistoryRepository extends JpaRepository<PostHistory, Long> {

}
