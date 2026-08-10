package com.spring.spotlog_api.domain.reservation.dto;

import java.time.LocalDate;
import java.util.List;

public record AvailableSlotsResponse(
    LocalDate date,
    List<SlotResponse> slots
) { }