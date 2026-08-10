package com.spring.spotlog_api.domain.reservationoption;

import com.spring.spotlog_api.domain.reservation.dto.AvailableSlotsResponse;
import com.spring.spotlog_api.domain.reservationoption.dto.ReservationOptionCreateRequest;
import com.spring.spotlog_api.domain.reservationoption.dto.ReservationOptionResponse;
import com.spring.spotlog_api.domain.reservationoption.dto.ReservationOptionUpdateRequest;
import com.spring.spotlog_api.global.auth.LoginMember;
import com.spring.spotlog_api.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/places/{placeId}/options")
@RequiredArgsConstructor
public class ReservationOptionController {
    private final ReservationOptionService optionService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<ReservationOptionResponse>> create(
            @LoginMember UUID memberId,
            @PathVariable UUID placeId,
            @Valid @RequestBody ReservationOptionCreateRequest request
    ) {
        ReservationOptionResponse response = optionService.create(memberId, placeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("예약 옵션이 등록되었습니다.", response));
    }

    // 공개용 - ACTIVE만
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationOptionResponse>>> findAll(
            @PathVariable UUID placeId
    ) {
        List<ReservationOptionResponse> response = optionService.findActiveByPlace(placeId);
        return ResponseEntity.ok(ApiResponse.ok("예약 옵션 목록 조회에 성공했습니다.", response));
    }

    // OWNER 전용 - 전체(ACTIVE + INACTIVE)
    @GetMapping("/my")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<ReservationOptionResponse>>> findAllForOwner(
            @LoginMember UUID memberId,
            @PathVariable UUID placeId
    ) {
        List<ReservationOptionResponse> response = optionService.findAllByPlaceForOwner(memberId, placeId);
        return ResponseEntity.ok(ApiResponse.ok("예약 옵션 전체 목록 조회에 성공했습니다.", response));
    }

    @GetMapping("/{optionId}")
    public ResponseEntity<ApiResponse<ReservationOptionResponse>> findOne(
            @PathVariable UUID placeId,
            @PathVariable UUID optionId
    ) {
        ReservationOptionResponse response = optionService.findOne(optionId);
        return ResponseEntity.ok(ApiResponse.ok("예약 옵션 상세 조회에 성공했습니다.", response));
    }

    @PatchMapping("/{optionId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<ReservationOptionResponse>> update(
            @LoginMember UUID memberId,
            @PathVariable UUID placeId,
            @PathVariable UUID optionId,
            @Valid @RequestBody ReservationOptionUpdateRequest request
    ) {
        ReservationOptionResponse response = optionService.update(memberId, optionId, request);
        return ResponseEntity.ok(ApiResponse.ok("예약 옵션이 수정되었습니다.", response));
    }

    @PatchMapping("/{optionId}/activate")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> activate(
            @LoginMember UUID memberId,
            @PathVariable UUID placeId,
            @PathVariable UUID optionId
    ) {
        optionService.activate(memberId, optionId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{optionId}/deactivate")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deactivate(
            @LoginMember UUID memberId,
            @PathVariable UUID placeId,
            @PathVariable UUID optionId
    ) {
        optionService.deactivate(memberId, optionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{optionId}/available-slots")
    public ResponseEntity<ApiResponse<AvailableSlotsResponse>> findAvailableSlots(
            @PathVariable UUID placeId,
            @PathVariable UUID optionId,
            @RequestParam LocalDate date
    ) {
        AvailableSlotsResponse response = optionService.findAvailableSlots(optionId, date);
        return ResponseEntity.ok(ApiResponse.ok("예약 가능 시간대 조회에 성공했습니다.", response));
    }
}