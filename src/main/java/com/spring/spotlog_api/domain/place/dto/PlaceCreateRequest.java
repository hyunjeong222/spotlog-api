package com.spring.spotlog_api.domain.place.dto;

import com.spring.spotlog_api.domain.place.PlaceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record PlaceCreateRequest (
    @NotNull(message = "카테고리는 필수입니다.")
    PlaceCategory category,

    @NotBlank(message = "장소명은 필수입니다.")
    @Size(max = 30, message = "장소명은 30자 이하여야 합니다.")
    String name,

    @Size(max = 255, message = "설명은 255자 이하여야 합니다.")
    String description,

    @NotBlank(message = "도로명 주소는 필수입니다.")
    String roadAddress,

    @NotBlank(message = "지번 주소는 필수입니다.")
    String jibunAddress,

    @NotBlank(message = "우편번호는 필수입니다.")
    @Size(max = 10, message = "우편번호는 10자 이하여야 합니다.")
    String postalCode,

    @NotNull(message = "위도는 필수입니다.")
    Double latitude,

    @NotNull(message = "경도는 필수입니다.")
    Double longitude,

    @NotNull(message = "영업 시작 시간은 필수입니다.")
    LocalTime openTime,

    @NotNull(message = "영업 종료 시간은 필수입니다.")
    LocalTime closeTime
) { }