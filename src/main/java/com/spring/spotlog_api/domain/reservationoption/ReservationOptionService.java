package com.spring.spotlog_api.domain.reservationoption;

import com.spring.spotlog_api.domain.place.Place;
import com.spring.spotlog_api.domain.place.PlaceRepository;
import com.spring.spotlog_api.domain.reservation.Reservation;
import com.spring.spotlog_api.domain.reservation.ReservationRepository;
import com.spring.spotlog_api.domain.reservation.ReservationStatus;
import com.spring.spotlog_api.domain.reservation.dto.AvailableSlotsResponse;
import com.spring.spotlog_api.domain.reservation.dto.SlotResponse;
import com.spring.spotlog_api.domain.reservationoption.dto.ReservationOptionCreateRequest;
import com.spring.spotlog_api.domain.reservationoption.dto.ReservationOptionResponse;
import com.spring.spotlog_api.domain.reservationoption.dto.ReservationOptionUpdateRequest;
import com.spring.spotlog_api.global.exception.CustomException;
import com.spring.spotlog_api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationOptionService {
    private final ReservationOptionRepository optionRepository;
    private final PlaceRepository placeRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public ReservationOptionResponse create(UUID memberId, UUID placeId, ReservationOptionCreateRequest request) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        if (!place.getOwner().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.NO_PLACE_PERMISSION);
        }

        ReservationOption option = ReservationOption.create(
                place, request.name(), request.description(), request.capacity(), request.slotDurationMinutes()
        );
        return ReservationOptionResponse.from(optionRepository.save(option));
    }

    // 공개용 - ACTIVE만
    @Transactional(readOnly = true)
    public List<ReservationOptionResponse> findActiveByPlace(UUID placeId) {
        if (!placeRepository.existsById(placeId)) {
            throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
        }
        return optionRepository.findByPlace_IdAndStatus(placeId, ReservationOptionStatus.ACTIVE).stream()
                .map(ReservationOptionResponse::from)
                .toList();
    }

    // OWNER 전용 - 전체(ACTIVE + INACTIVE)
    @Transactional(readOnly = true)
    public List<ReservationOptionResponse> findAllByPlaceForOwner(UUID memberId, UUID placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        if (!place.getOwner().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.NO_PLACE_PERMISSION);
        }
        return optionRepository.findByPlace_Id(placeId).stream()
                .map(ReservationOptionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservationOptionResponse findOne(UUID optionId) {
        ReservationOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPTION_NOT_FOUND));
        return ReservationOptionResponse.from(option);
    }

    @Transactional
    public ReservationOptionResponse update(UUID memberId, UUID optionId, ReservationOptionUpdateRequest request) {
        ReservationOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPTION_NOT_FOUND));

        if (!option.getPlace().getOwner().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.NO_PLACE_PERMISSION);
        }
        option.update(request.name(), request.description(), request.capacity(), request.slotDurationMinutes());
        return ReservationOptionResponse.from(option);
    }

    @Transactional
    public void activate(UUID memberId, UUID optionId) {
        ReservationOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPTION_NOT_FOUND));

        if (!option.getPlace().getOwner().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.NO_PLACE_PERMISSION);
        }
        option.activate();
    }

    @Transactional
    public void deactivate(UUID memberId, UUID optionId) {
        ReservationOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPTION_NOT_FOUND));

        if (!option.getPlace().getOwner().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.NO_PLACE_PERMISSION);
        }
        option.deactivate();
    }

    // 슬롯 생성
    @Transactional(readOnly = true)
    public AvailableSlotsResponse findAvailableSlots(UUID optionId, LocalDate date) {
        ReservationOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPTION_NOT_FOUND));

        if (option.getStatus() == ReservationOptionStatus.INACTIVE) {
            throw new CustomException(ErrorCode.INACTIVE_OPTION);
        }

        Place place = option.getPlace();

        List<LocalTime> allSlots = generateSlots(
                place.getOpenTime(), place.getCloseTime(), option.getSlotDurationMinutes()
        );

        Set<LocalTime> reservedTimes = reservationRepository
                .findByOption_IdAndReservationDateAndStatus(optionId, date, ReservationStatus.RESERVED)
                .stream()
                .map(Reservation::getStartTime)
                .collect(Collectors.toSet());

        List<SlotResponse> slots = allSlots.stream()
                .map(time -> new SlotResponse(time, !reservedTimes.contains(time)))
                .toList();

        return new AvailableSlotsResponse(date, slots);
    }

    private List<LocalTime> generateSlots(LocalTime open, LocalTime close, int durationMinutes) {
        List<LocalTime> result = new ArrayList<>();
        LocalTime cursor = open;
        while (!cursor.plusMinutes(durationMinutes).isAfter(close)) {
            result.add(cursor);
            cursor = cursor.plusMinutes(durationMinutes);
        }
        return result;
    }
}