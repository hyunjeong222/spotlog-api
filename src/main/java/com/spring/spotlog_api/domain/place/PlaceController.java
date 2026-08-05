package com.spring.spotlog_api.domain.place;

import com.spring.spotlog_api.domain.place.dto.*;
import com.spring.spotlog_api.global.common.ApiResponse;
import com.spring.spotlog_api.global.common.PageResponse;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {
    private final PlaceService placeService;

    // 장소 등록
    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<PlaceCreateResponse>> create(
            @AuthenticationPrincipal String memberId,
            @RequestBody @Valid PlaceCreateRequest request) {
        PlaceCreateResponse response = placeService.create(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("장소가 등록되었습니다.", response));
    }

    // 장소 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PlaceListResponse>>> getPlaces(
            @RequestParam(required = false) PlaceCategory category,
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC)
            @Parameter(hidden = true) Pageable pageable) {
        PageResponse<PlaceListResponse> response =
                placeService.getPlaces(category, pageable);
        return ResponseEntity.ok(ApiResponse.ok("장소 목록 조회 성공", response));
    }

    // 장소 상세 조회
    @GetMapping("/{placeId}")
    public ResponseEntity<ApiResponse<PlaceDetailResponse>> getPlace(
            @PathVariable UUID placeId) {
        PlaceDetailResponse response = placeService.getPlace(placeId);
        return ResponseEntity.ok(ApiResponse.ok("장소 상세 조회 성공", response));
    }

    // 본인 장소 목록 조회 (사업자 회원)
    @GetMapping("/my")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<PageResponse<PlaceListResponse>>> getMyPlaces(
            @AuthenticationPrincipal String memberId,
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC)
            @Parameter(hidden = true) Pageable pageable) {
        PageResponse<PlaceListResponse> response =
                placeService.getMyPlaces(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.ok("내 장소 목록 조회 성공", response));
    }

    // 장소 활성화
    @PatchMapping("/{placeId}/activate")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> activate(
            @AuthenticationPrincipal String memberId,
            @PathVariable UUID placeId) {
        placeService.activate(memberId, placeId);
        return ResponseEntity.noContent().build();
    }

    // 장소 비활성화
    @PatchMapping("/{placeId}/deactivate")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deactivate(
            @AuthenticationPrincipal String memberId,
            @PathVariable UUID placeId) {
        placeService.deactivate(memberId, placeId);
        return ResponseEntity.noContent().build();
    }

    // 장소 수정
    @PatchMapping("/{placeId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<PlaceDetailResponse>> update(
            @AuthenticationPrincipal String memberId,
            @PathVariable UUID placeId,
            @RequestBody @Valid PlaceUpdateRequest request) {
        PlaceDetailResponse response = placeService.update(memberId, placeId, request);
        return ResponseEntity.ok(ApiResponse.ok("장소가 수정되었습니다.", response));
    }

    // 장소 삭제
    @DeleteMapping("/{placeId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal String memberId,
            @PathVariable UUID placeId) {
        placeService.delete(memberId, placeId);
        return ResponseEntity.noContent().build();
    }
}