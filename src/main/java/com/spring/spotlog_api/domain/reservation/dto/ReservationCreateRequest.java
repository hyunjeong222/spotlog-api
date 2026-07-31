package com.spring.spotlog_api.domain.reservation.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationCreateRequest(
    @NotNull Long optionId,
    @NotNull @Future LocalDate reservationDate,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime
) { }