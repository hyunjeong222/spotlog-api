package com.spring.spotlog_api.domain.owner;

import com.spring.spotlog_api.domain.owner.dto.OwnerApplicationResponse;
import com.spring.spotlog_api.domain.owner.dto.RejectRequest;
import com.spring.spotlog_api.global.common.ApiResponse;
import com.spring.spotlog_api.global.common.PageResponse;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/owner-applications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOwnerApplicationController {
    private final OwnerApplicationService ownerApplicationService;

    // 신청 목록 조회 (상태 필터)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OwnerApplicationResponse>>> getApplications(
            @RequestParam(required = false) ApplicationStatus status,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            @Parameter(hidden = true) Pageable pageable) {
        PageResponse<OwnerApplicationResponse> response =
                ownerApplicationService.getApplications(status, pageable);
        return ResponseEntity.ok(ApiResponse.ok("신청 목록 조회 성공", response));
    }

    // 상세 조회
    @GetMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<OwnerApplicationResponse>> getApplication(
            @PathVariable UUID applicationId) {
        OwnerApplicationResponse response =
                ownerApplicationService.getApplication(applicationId);
        return ResponseEntity.ok(ApiResponse.ok("신청 상세 조회 성공", response));
    }

    // 승인
    @PostMapping("/{applicationId}/approve")
    public ResponseEntity<Void> approve(@PathVariable UUID applicationId) {
        ownerApplicationService.approve(applicationId);
        return ResponseEntity.noContent().build();
    }

    // 거절
    @PostMapping("/{applicationId}/reject")
    public ResponseEntity<Void> reject(
            @PathVariable UUID applicationId,
            @RequestBody @Valid RejectRequest request) {
        ownerApplicationService.reject(applicationId, request.reason());
        return ResponseEntity.noContent().build();
    }

    // 강제 권한 회수
    @DeleteMapping("/members/{memberId}/owner-role")
    public ResponseEntity<Void> revokeOwner(@PathVariable UUID memberId) {
        ownerApplicationService.revokeOwner(memberId);
        return ResponseEntity.noContent().build();
    }
}