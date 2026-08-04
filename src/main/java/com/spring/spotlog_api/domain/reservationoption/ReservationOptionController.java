package com.spring.spotlog_api.domain.reservationoption;

import com.spring.spotlog_api.domain.reservationoption.dto.ReservationOptionCreateRequest;
import com.spring.spotlog_api.domain.reservationoption.dto.ReservationOptionResponse;
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

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationOptionResponse>>> findAll(
            @PathVariable UUID placeId
    ) {
        List<ReservationOptionResponse> response = optionService.findByPlace(placeId);
        return ResponseEntity.ok(ApiResponse.ok("예약 옵션 목록 조회에 성공했습니다.", response));
    }
}