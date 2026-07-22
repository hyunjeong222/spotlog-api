package com.spring.spotlog_api.domain.owner.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectRequest (
        @NotBlank(message = "거절 사유는 필수입니다.")
        String reason
) {}