package com.spring.spotlog_api.domain.reservationoption.dto;

import com.spring.spotlog_api.domain.reservationoption.ReservationOption;
import com.spring.spotlog_api.domain.reservationoption.ReservationOptionStatus;

import java.util.UUID;

public record ReservationOptionResponse(
    UUID id,
    UUID placeId,
    String name,
    Integer capacity,
    Integer slotDurationMinutes,
    ReservationOptionStatus status
) {
    public static ReservationOptionResponse from(ReservationOption option) {
        return new ReservationOptionResponse(
                option.getId(),
                option.getPlace().getId(),
                option.getName(),
                option.getCapacity(),
                option.getSlotDurationMinutes(),
                option.getStatus()
        );
    }
}