package com.spring.spotlog_api.domain.place.dto;

import com.spring.spotlog_api.domain.place.Place;
import com.spring.spotlog_api.domain.place.PlaceCategory;
import com.spring.spotlog_api.domain.place.PlaceStatus;

import java.time.LocalTime;
import java.util.UUID;

public record PlaceListResponse (
    UUID id,
    String name,
    PlaceCategory category,
    String roadAddress,
    LocalTime openTime,
    LocalTime closeTime,
    PlaceStatus status,
    boolean isOpenNow
) {
    public static PlaceListResponse from(Place place) {
        return new PlaceListResponse(
                place.getId(),
                place.getName(),
                place.getCategory(),
                place.getRoadAddress(),
                place.getOpenTime(),
                place.getCloseTime(),
                place.getStatus(),
                place.isOpenNow()
        );
    }
}