package com.spring.spotlog_api.domain.place.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record PlaceUpdateRequest(
    @Size(max = 30, message = "장소명은 30자 이하여야 합니다.")
    String name,

    @Size(max = 255, message = "설명은 255자 이하여야 합니다.")
    String description,

    String roadAddress,
    String jibunAddress,

    @Size(max = 10, message = "우편번호는 10자 이하여야 합니다.")
    String postalCode,

    Double latitude,
    Double longitude,

    LocalTime openTime,
    LocalTime closeTime
) { }