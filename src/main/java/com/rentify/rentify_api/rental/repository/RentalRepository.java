package com.rentify.rentify_api.rental.repository;

import com.rentify.rentify_api.rental.entity.Rental;
import com.rentify.rentify_api.rental.entity.RentalStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Rental r WHERE r.id = :id")
    Optional<Rental> findByIdWithPessimisticLock(@Param("id") Long id);

    @Query("SELECT r FROM Rental r WHERE r.post.id = :postId " +
           "AND r.status IN :statuses " +
           "AND ((r.startDate <= :endDate AND r.endDate >= :startDate))")
    List<Rental> findOverlappingRentals(
        @Param("postId") Long postId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("statuses") List<RentalStatus> statuses
    );

    // 내가 빌린 대여 목록
    @Query(
        value = "SELECT r, p FROM Rental r " +
                "JOIN FETCH r.post po " +
                "JOIN FETCH po.user u " +
                "LEFT JOIN Payment p ON p.rental = r " +
                "AND p.id = (SELECT MAX(p2.id) FROM Payment p2 WHERE p2.rental = r) " +
                "WHERE r.user.id = :userId " +
                "ORDER BY r.createdAt DESC ",
        countQuery = "SELECT count(r) FROM Rental r WHERE r.user.id = :userId"
    )
    Page<Object[]> findByUserId(@Param("userId") Long userId, Pageable pageable);

    // 내가 빌려준 대여 목록
    @Query(
        value = "SELECT r, p FROM Rental r " +
                "JOIN FETCH r.post po " +
                "JOIN FETCH po.user u " +
                "LEFT JOIN Payment p ON p.rental = r " +
                "AND p.id = (SELECT MAX(p2.id) FROM Payment p2 WHERE p2.rental = r) " +
                "WHERE po.user.id = :userId " +
                "ORDER BY r.createdAt DESC ",
        countQuery = "SELECT count(r) FROM Rental r JOIN r.post po WHERE po.user.id = :userId"
    )
    Page<Object[]> findByPostOwnerId(@Param("userId") Long userId, Pageable pageable);

    // 나의 모든 대여 목록
    @Query(
        value = "SELECT r, p FROM Rental r " +
                "JOIN FETCH r.post po " +
                "JOIN FETCH po.user u " +
                "LEFT JOIN Payment p ON p.rental = r " +
                "AND p.id = (SELECT MAX(p2.id) FROM Payment p2 WHERE p2.rental = r) " +
                "WHERE r.user.id = :userId OR po.user.id = :userId " +
                "ORDER BY r.createdAt DESC ",
        countQuery = "SELECT count(r) FROM Rental r JOIN r.post po WHERE r.user.id = :userId OR po.user.id = :userId"
    )
    Page<Object[]> findByUserIdOrPostOwnerId(@Param("userId") Long userId, Pageable pageable);
}
