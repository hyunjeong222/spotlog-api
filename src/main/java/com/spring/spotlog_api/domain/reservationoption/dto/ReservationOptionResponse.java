package com.spring.spotlog_api.domain.reservationoption.dto;

import com.spring.spotlog_api.domain.reservationoption.ReservationOption;
import com.spring.spotlog_api.domain.reservationoption.ReservationOptionStatus;

import java.time.LocalTime;
import java.util.UUID;

public record ReservationOptionResponse(
    UUID id,
    UUID placeId,
    String name,
    String description,
    Integer capacity,
    Integer slotDurationMinutes,
    LocalTime availableStartTime,
    LocalTime availableEndTime,
    ReservationOptionStatus status
) {
    public static ReservationOptionResponse from(ReservationOption option) {
        return new ReservationOptionResponse(
                option.getId(), option.getPlace().getId(), option.getName(),
                option.getDescription(), option.getCapacity(), option.getSlotDurationMinutes(),
                option.getAvailableStartTime(), option.getAvailableEndTime(), option.getStatus()
        );
    }
}