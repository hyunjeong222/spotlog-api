package com.spring.spotlog_api.domain.owner.dto;

import com.spring.spotlog_api.domain.owner.ApplicationStatus;
import com.spring.spotlog_api.domain.owner.OwnerApplication;

import java.time.LocalDateTime;
import java.util.UUID;

public record OwnerApplicationResponse (
        UUID id,
        String businessNumber,
        ApplicationStatus status,
        String reason,
        LocalDateTime createdAt,
        LocalDateTime processedAt
) {
    public static OwnerApplicationResponse from(OwnerApplication application) {
        return new OwnerApplicationResponse(
                application.getId(),
                application.getBusinessNumber(),
                application.getStatus(),
                application.getReason(),
                application.getCreatedAt(),
                application.getProcessedAt()
        );
    }
}