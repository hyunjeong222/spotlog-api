package com.spring.spotlog_api.domain.reservation.controller;

import com.spring.spotlog_api.domain.reservation.dto.ReservationCreateRequest;
import com.spring.spotlog_api.domain.reservation.dto.ReservationResponse;
import com.spring.spotlog_api.domain.reservation.service.RedisLockReservationService;
import com.spring.spotlog_api.global.auth.LoginMember;
import com.spring.spotlog_api.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reservations/redis-lock")
@RequiredArgsConstructor
public class RedisLockReservationController {
    private final RedisLockReservationService reservationService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<ReservationResponse>> reserve(
            @LoginMember UUID memberId,
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        ReservationResponse response = reservationService.reserve(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("예약이 완료되었습니다.", response));
    }
}