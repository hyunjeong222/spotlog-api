package com.spring.spotlog_api.domain.owner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OwnerApplicationRequest (
    @NotBlank(message = "사업자등록번호는 필수입니다.")
    @Pattern(
        regexp = "^\\d{3}-\\d{2}-\\d{5}$",
        message = "사업자등록번호 형식이 올바르지 않습니다. (예: 123-45-67890)"
    )
    String businessNumber
) {}