package com.spring.spotlog_api.domain.reservation.controller;

import com.spring.spotlog_api.domain.reservation.dto.ReservationCreateRequest;
import com.spring.spotlog_api.domain.reservation.dto.ReservationResponse;
import com.spring.spotlog_api.domain.reservation.service.PessimisticReservationService;
import com.spring.spotlog_api.global.auth.LoginMember;
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
    public ResponseEntity<ReservationResponse> reserve(
            @LoginMember UUID memberId,
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        ReservationResponse response = reservationService.reserve(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<ReservationResponse>> findMyReservations(@LoginMember UUID memberId) {
        return ResponseEntity.ok(reservationService.findMyReservations(memberId));
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> findOne(
            @LoginMember UUID memberId,
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(reservationService.findOne(memberId, reservationId));
    }

    @DeleteMapping("/{reservationId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> cancel(
            @LoginMember UUID memberId,
            @PathVariable Long reservationId
    ) {
        reservationService.cancel(memberId, reservationId);
        return ResponseEntity.noContent().build();
    }
}