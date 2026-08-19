package com.spring.spotlog_api.domain.reservation.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReservationCreateRequest(
    @NotNull UUID optionId,
    @NotNull @Future(message = "예약 가능한 날짜가 아닙니다") LocalDate reservationDate,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    @NotNull @Positive Integer quantity
) { }