package com.rentify.rentify_api.point.repository;

import com.rentify.rentify_api.payment.entity.Payment;
import com.rentify.rentify_api.point.entity.PointHistory;
import com.rentify.rentify_api.point.entity.PointHistoryType;
import com.rentify.rentify_api.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    Optional<PointHistory> findByPaymentAndType(Payment payment, PointHistoryType earn);

    List<PointHistory> findAllByUserOrderByCreateAtDesc(User user);
}
