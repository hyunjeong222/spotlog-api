package com.spring.spotlog_api.domain.owner;

import com.spring.spotlog_api.domain.owner.dto.OwnerApplicationRequest;
import com.spring.spotlog_api.domain.owner.dto.OwnerApplicationResponse;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/owner-applications")
@RequiredArgsConstructor
public class OwnerApplicationController {
    private final OwnerApplicationService ownerApplicationService;

    // 사업자 전환 신청
    @PostMapping
    public ResponseEntity<ApiResponse<OwnerApplicationResponse>> apply(
            @AuthenticationPrincipal String memberId,
            @RequestBody @Valid OwnerApplicationRequest request) {
        OwnerApplicationResponse response = ownerApplicationService.apply(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("사업자 전환 신청이 완료되었습니다.", response));
    }

    // 본인 신청 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OwnerApplicationResponse>>> getMyApplications(
            @AuthenticationPrincipal String memberId,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            @Parameter(hidden = true) Pageable pageable) {
        PageResponse<OwnerApplicationResponse> response =
                ownerApplicationService.getMyApplications(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.ok("신청 목록 조회 성공", response));
    }

    // 본인 신청 상세 조회
    @GetMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<OwnerApplicationResponse>> getMyApplication(
            @AuthenticationPrincipal String memberId,
            @PathVariable UUID applicationId) {
        OwnerApplicationResponse response =
                ownerApplicationService.getMyApplication(memberId, applicationId);
        return ResponseEntity.ok(ApiResponse.ok("신청 상세 조회 성공", response));
    }


    // 신청 취소
    @DeleteMapping("/{applicationId}")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal String memberId,
            @PathVariable UUID applicationId) {
        ownerApplicationService.cancel(memberId, applicationId);
        return ResponseEntity.noContent().build();
    }
}