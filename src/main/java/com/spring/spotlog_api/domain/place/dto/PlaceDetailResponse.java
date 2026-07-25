package com.spring.spotlog_api.domain.place.dto;

import com.spring.spotlog_api.domain.place.Place;
import com.spring.spotlog_api.domain.place.PlaceCategory;
import com.spring.spotlog_api.domain.place.PlaceStatus;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record PlaceDetailResponse (
    UUID id,
    String name,
    PlaceCategory category,
    String description,
    String roadAddress,
    String jibunAddress,
    String postalCode,
    Double latitude,
    Double longitude,
    LocalTime openTime,
    LocalTime closeTime,
    PlaceStatus status,
    boolean isOpenNow,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static PlaceDetailResponse from(Place place) {
        return new PlaceDetailResponse(
                place.getId(),
                place.getName(),
                place.getCategory(),
                place.getDescription(),
                place.getRoadAddress(),
                place.getJibunAddress(),
                place.getPostalCode(),
                place.getLatitude(),
                place.getLongitude(),
                place.getOpenTime(),
                place.getCloseTime(),
                place.getStatus(),
                place.isOpenNow(),
                place.getCreatedAt(),
                place.getUpdatedAt()
        );
    }
}