package com.spring.spotlog_api.domain.reservation.controller;

import com.spring.spotlog_api.domain.reservation.dto.ReservationCreateRequest;
import com.spring.spotlog_api.domain.reservation.dto.ReservationResponse;
import com.spring.spotlog_api.domain.reservation.service.PessimisticReservationService;
import com.spring.spotlog_api.global.auth.LoginMember;
import com.spring.spotlog_api.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations/pessimistic")
@RequiredArgsConstructor
public class ReservationController {
    private final PessimisticReservationService reservationService;

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

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> findMyReservations(
            @LoginMember UUID memberId
    ) {
        List<ReservationResponse> response = reservationService.findMyReservations(memberId);
        return ResponseEntity.ok(ApiResponse.ok("내 예약 목록 조회에 성공했습니다.", response));
    }

    @GetMapping("/{reservationId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<ReservationResponse>> findOne(
            @LoginMember UUID memberId,
            @PathVariable UUID reservationId
    ) {
        ReservationResponse response = reservationService.findOne(memberId, reservationId);
        return ResponseEntity.ok(ApiResponse.ok("예약 상세 조회에 성공했습니다.", response));
    }

    @DeleteMapping("/{reservationId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> cancel(
            @LoginMember UUID memberId,
            @PathVariable UUID reservationId
    ) {
        reservationService.cancel(memberId, reservationId);
        return ResponseEntity.noContent().build();
    }
}