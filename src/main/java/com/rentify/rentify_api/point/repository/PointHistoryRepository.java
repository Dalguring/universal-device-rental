package com.rentify.rentify_api.point.repository;

import com.rentify.rentify_api.point.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

}
