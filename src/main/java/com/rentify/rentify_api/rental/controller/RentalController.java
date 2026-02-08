package com.rentify.rentify_api.rental.controller;

import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.rental.dto.RentalRequest;
import com.rentify.rentify_api.rental.dto.RentalResponse;
import com.rentify.rentify_api.rental.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rentals")
public class RentalController implements RentalApiDocs {

    private final RentalService rentalService;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<RentalResponse>> createRental(
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody RentalRequest request
    ) {
        RentalResponse response = rentalService.createRental(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED, response));
    }

    @Override
    @PostMapping("/{rentalId}/confirm")
    public ResponseEntity<ApiResponse<RentalResponse>> confirmRental(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long rentalId
    ) {
        RentalResponse response = rentalService.confirmRental(userId, rentalId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, response));
    }

    @Override
    @PostMapping("/{rentalId}/cancel")
    public ResponseEntity<ApiResponse<RentalResponse>> cancelRental(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long rentalId
    ) {
        RentalResponse response = rentalService.cancelRental(userId, rentalId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, response));
    }
}
