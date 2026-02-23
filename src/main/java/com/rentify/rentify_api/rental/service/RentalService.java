package com.rentify.rentify_api.rental.service;

import com.rentify.rentify_api.common.exception.NotFoundException;
import com.rentify.rentify_api.post.entity.Post;
import com.rentify.rentify_api.post.entity.PostStatus;
import com.rentify.rentify_api.post.repository.PostRepository;
import com.rentify.rentify_api.rental.dto.RentalRequest;
import com.rentify.rentify_api.rental.dto.RentalResponse;
import com.rentify.rentify_api.rental.entity.ReceiveMethod;
import com.rentify.rentify_api.rental.entity.Rental;
import com.rentify.rentify_api.rental.entity.RentalStatus;
import com.rentify.rentify_api.rental.exception.RentalNotAvailableException;
import com.rentify.rentify_api.rental.exception.RentalNotFoundException;
import com.rentify.rentify_api.rental.repository.RentalRepository;
import com.rentify.rentify_api.user.entity.User;
import com.rentify.rentify_api.user.exception.UserNotFoundException;
import com.rentify.rentify_api.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RentalService {

    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public RentalResponse createRental(Long userId, RentalRequest request) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 게시글 조회
        Post post = postRepository.findById(request.getPostId())
            .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));

        // 본인의 게시글 여부 확인
        if (post.getUser().getId().equals(userId)) {
            throw new RentalNotAvailableException("본인의 게시글은 대여할 수 없습니다.");
        }

        // 유효성 검증
        validateRentalRequest(request, post);

        // 대여 기간 중복 체크
        checkRentalAvailability(request.getPostId(), request.getStartDate(), request.getEndDate());

        // 전체 금액 계산
        int totalPrice = calculateTotalPrice(post.getPricePerDay(), request.getStartDate(), request.getEndDate());

        // 대여 정보 생성
        Rental rental = Rental.builder()
            .user(user)
            .post(post)
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .receiveMethod(request.getReceiveMethod())
            .status(RentalStatus.REQUESTED)
            .totalPrice(totalPrice)
            .build();

        Rental savedRental = rentalRepository.save(rental);

        // 대여 신청 단계에서는 게시글 상태 변경하지 않음 (결제 확정 시 변경)
        return convertToResponse(savedRental);
    }

    private void validateRentalRequest(RentalRequest request, Post post) {
        // 게시글 상태 확인
        if (post.getStatus() != PostStatus.AVAILABLE) {
            throw new RentalNotAvailableException("현재 대여할 수 없는 게시글입니다.");
        }

        // 날짜 유효성 검증
        LocalDate today = LocalDate.now();
        if (request.getStartDate().isBefore(today)) {
            throw new RentalNotAvailableException("대여 시작일은 오늘 이후여야 합니다.");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RentalNotAvailableException("대여 종료일은 시작일 이후여야 합니다.");
        }

        // 최대 대여 기간 확인
        long rentalDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        if (rentalDays > post.getMaxRentalDays()) {
            throw new RentalNotAvailableException(
                String.format("최대 대여 기간(%d일)을 초과했습니다.", post.getMaxRentalDays())
            );
        }

        // 수령 방법 확인
        if (request.getReceiveMethod() == ReceiveMethod.PARCEL && !post.getIsParcel()) {
            throw new RentalNotAvailableException("택배 수령이 불가능한 게시글입니다.");
        }

        if (request.getReceiveMethod() == ReceiveMethod.MEETUP && !post.getIsMeetup()) {
            throw new RentalNotAvailableException("직거래가 불가능한 게시글입니다.");
        }
    }

    private void checkRentalAvailability(Long postId, LocalDate startDate, LocalDate endDate) {
        List<RentalStatus> activeStatuses = List.of(
            RentalStatus.REQUESTED,
            RentalStatus.CONFIRMED,
            RentalStatus.IN_USE
        );

        List<Rental> overlappingRentals = rentalRepository.findOverlappingRentals(
            postId, startDate, endDate, activeStatuses
        );

        if (!overlappingRentals.isEmpty()) {
            throw new RentalNotAvailableException("해당 기간에 이미 대여가 진행 중이거나 예정되어 있습니다.");
        }
    }

    private int calculateTotalPrice(int pricePerDay, LocalDate startDate, LocalDate endDate) {
        long rentalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return (int) (pricePerDay * rentalDays);
    }

    private RentalResponse convertToResponse(Rental rental) {
        return RentalResponse.builder()
            .rentalId(rental.getId())
            .userId(rental.getUser().getId())
            .postId(rental.getPost().getId())
            .startDate(rental.getStartDate())
            .endDate(rental.getEndDate())
            .receiveMethod(rental.getReceiveMethod())
            .status(rental.getStatus())
            .totalPrice(rental.getTotalPrice())
            .createdAt(rental.getCreatedAt())
            .updatedAt(rental.getUpdatedAt())
            .build();
    }

    @Transactional
    public RentalResponse confirmRental(Long userId, Long rentalId) {
        // 대여 정보 조회
        Rental rental = rentalRepository.findById(rentalId)
            .orElseThrow(RentalNotFoundException::new);

        // 본인 확인
        if (!rental.getUser().getId().equals(userId)) {
            throw new RentalNotAvailableException("본인의 대여만 확정할 수 있습니다.");
        }

        // TODO: 결제 처리 로직 (추후 구현)
        // Payment payment = paymentService.processPayment(rental);

        // 대여 확정
        rental.confirm();

        // 게시글 상태를 RESERVED로 변경
        rental.getPost().updateStatus(PostStatus.RESERVED);

        return convertToResponse(rental);
    }

    @Transactional
    public RentalResponse cancelRental(Long userId, Long rentalId) {
        // 대여 정보 조회
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(RentalNotFoundException::new);

        // 본인 확인
        if (!rental.getUser().getId().equals(userId)) {
            throw new RentalNotAvailableException("본인의 대여만 취소할 수 있습니다.");
        }

        // TODO: 결제 취소 로직 (추후 구현)
        // if (rental.getStatus() == RentalStatus.CONFIRMED) {
        //     paymentService.refundPayment(rental);
        // }

        // 대여 취소
        rental.cancel();

        // 게시글이 RESERVED 상태였다면 AVAILABLE로 변경
        if (rental.getPost().getStatus() == PostStatus.RESERVED) {
            rental.getPost().updateStatus(PostStatus.AVAILABLE);
        }

        return convertToResponse(rental);
    }

    // 내가 빌리는 대여 목록
    @Transactional(readOnly = true)
    public Page<RentalResponse> getMyBorrowedRentals(Long userId, Pageable pageable) {
        return rentalRepository.findByUserId(userId, pageable);
    }

    // 내가 빌려준 대여 목록
    @Transactional(readOnly = true)
    public Page<RentalResponse> getMyLendedRentals(Long userId, Pageable pageable) {
        return rentalRepository.findByPostOwnerId(userId, pageable);
    }

    // 나의 모든 대여 목록
    @Transactional(readOnly = true)
    public Page<RentalResponse> getMyAllRentals(Long userId, Pageable pageable) {
        return rentalRepository.findByUserIdOrPostOwnerId(userId, pageable);
    }
}
