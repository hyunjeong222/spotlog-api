package com.spring.spotlog_api.domain.reservation.dto;

import com.spring.spotlog_api.domain.reservation.Reservation;
import com.spring.spotlog_api.domain.reservation.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReservationResponse(
    UUID id,
    UUID optionId,
    LocalDate reservationDate,
    LocalTime startTime,
    LocalTime endTime,
    Integer quantity,
    ReservationStatus status
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getOption().getId(),
                reservation.getReservationDate(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getQuantity(),
                reservation.getStatus()
        );
    }
}