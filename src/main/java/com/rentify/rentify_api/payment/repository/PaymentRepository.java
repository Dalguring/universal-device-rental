package com.rentify.rentify_api.payment.repository;

import com.rentify.rentify_api.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query(
        value =
            "SELECT p FROM Payment p " +
            "JOIN FETCH p.user u " +
            "WHERE p.user.id = :userId",
        countQuery =
            "SELECT count(p) FROM Payment p " +
            "WHERE p.user.id = :userId"
    )
    Page<Payment> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
