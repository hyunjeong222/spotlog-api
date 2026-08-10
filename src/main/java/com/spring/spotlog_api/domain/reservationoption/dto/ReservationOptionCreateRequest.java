package com.spring.spotlog_api.domain.reservationoption.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationOptionCreateRequest(
    @NotBlank String name,
    String description,
    @NotNull @Positive Integer capacity,
    @NotNull @Positive Integer slotDurationMinutes
) { }