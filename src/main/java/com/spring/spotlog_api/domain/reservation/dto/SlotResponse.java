package com.spring.spotlog_api.domain.reservation.dto;

import java.time.LocalTime;

public record SlotResponse (
    LocalTime startTime,
    boolean available
) { }