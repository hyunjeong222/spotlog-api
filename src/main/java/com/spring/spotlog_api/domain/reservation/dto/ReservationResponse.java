package com.spring.spotlog_api.domain.reservation.dto;

import com.spring.spotlog_api.domain.reservation.Reservation;
import com.spring.spotlog_api.domain.reservation.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationResponse(
    Long id,
    Long optionId,
    LocalDate reservationDate,
    LocalTime startTime,
    LocalTime endTime,
    ReservationStatus status
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getOption().getId(),
                reservation.getReservationDate(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus()
        );
    }
}