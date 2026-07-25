package com.spring.spotlog_api.domain.place.dto;

import com.spring.spotlog_api.domain.place.Place;
import com.spring.spotlog_api.domain.place.PlaceCategory;
import com.spring.spotlog_api.domain.place.PlaceStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlaceCreateResponse (
    UUID id,
    String name,
    PlaceCategory category,
    PlaceStatus status,
    LocalDateTime createdAt
) {
    public static PlaceCreateResponse from(Place place) {
        return new PlaceCreateResponse(
                place.getId(),
                place.getName(),
                place.getCategory(),
                place.getStatus(),
                place.getCreatedAt()
        );
    }
}