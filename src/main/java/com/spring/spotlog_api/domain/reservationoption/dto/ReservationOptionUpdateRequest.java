package com.spring.spotlog_api.domain.reservationoption.dto;

import jakarta.validation.constraints.Positive;

import java.time.LocalTime;

public record ReservationOptionUpdateRequest(
    String name,
    String description,
    @Positive Integer capacity,
    @Positive Integer slotDurationMinutes,
    LocalTime availableStartTime,
    LocalTime availableEndTime
) { }