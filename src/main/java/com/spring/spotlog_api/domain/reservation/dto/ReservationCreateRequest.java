package com.spring.spotlog_api.domain.reservation.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReservationCreateRequest(
    @NotNull UUID optionId,
    @NotNull @Future LocalDate reservationDate,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime
) { }