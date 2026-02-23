package com.rentify.rentify_api.rental.repository;

import com.rentify.rentify_api.rental.dto.RentalResponse;
import com.rentify.rentify_api.rental.entity.Rental;
import com.rentify.rentify_api.rental.entity.RentalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {

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
            value = "SELECT r FROM Rental r " +
                    "JOIN FETCH r.post p " +
                    "JOIN FETCH p.user u " +
                    "WHERE r.user.id = :userId " +
                    "ORDER BY r.createdAt DESC ",
            countQuery = "SELECT count(r) FROM Rental r WHERE r.user.id = :userId"
    ) Page<RentalResponse> findByUserId(@Param("userId") Long userId, Pageable pageable);

    // 내가 빌려준 대여 목록
    @Query(
            value = "SELECT r FROM Rental r " +
                    "JOIN FETCH r.post p " +
                    "JOIN FETCH p.user u " +
                    "WHERE p.user.id = :userId " +
                    "ORDER BY r.createdAt DESC ",
            countQuery = "SELECT count(r) FROM Rental r JOIN r.post p WHERE p.user.id = :userId"
    ) Page<RentalResponse> findByPostOwnerId(@Param("userId") Long userId, Pageable pageable);

    // 나의 모든 대여 목록
    @Query(
            value = "SELECT r FROM Rental r " +
                    "JOIN FETCH r.post p " +
                    "JOIN FETCH p.user u " +
                    "WHERE r.user.id = :userId OR p.user.id = :userId " +
                    "ORDER BY r.createdAt DESC ",
            countQuery = "SELECT count(r) FROM Rental r JOIN r.post p WHERE r.user.id = :userId OR p.user.id = :userId"
    ) Page<RentalResponse> findByUserIdOrPostOwnerId(@Param("userId") Long userId, Pageable pageable);
}
