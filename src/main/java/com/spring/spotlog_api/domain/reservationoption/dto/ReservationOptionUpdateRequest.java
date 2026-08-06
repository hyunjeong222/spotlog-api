package com.spring.spotlog_api.domain.reservationoption.dto;

import jakarta.validation.constraints.Positive;

public record ReservationOptionUpdateRequest(
    String name,
    @Positive Integer capacity,
    @Positive Integer slotDurationMinutes
) { }