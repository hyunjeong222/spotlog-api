package com.spring.spotlog_api.domain.reservationoption;

import com.spring.spotlog_api.domain.place.Place;
import com.spring.spotlog_api.domain.place.PlaceRepository;
import com.spring.spotlog_api.domain.reservationoption.dto.ReservationOptionCreateRequest;
import com.spring.spotlog_api.domain.reservationoption.dto.ReservationOptionResponse;
import com.spring.spotlog_api.global.exception.CustomException;
import com.spring.spotlog_api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationOptionService {
    private final ReservationOptionRepository optionRepository;
    private final PlaceRepository placeRepository;

    @Transactional
    public ReservationOptionResponse create(UUID memberId, UUID placeId, ReservationOptionCreateRequest request) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        if (!place.getOwner().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.NO_PLACE_PERMISSION);
        }

        ReservationOption option = ReservationOption.create(
                place, request.name(), request.capacity(), request.slotDurationMinutes()
        );
        return ReservationOptionResponse.from(optionRepository.save(option));
    }

    @Transactional(readOnly = true)
    public List<ReservationOptionResponse> findByPlace(UUID placeId) {
        if (!placeRepository.existsById(placeId)) {
            throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
        }
        return optionRepository.findByPlace_Id(placeId).stream()
                .map(ReservationOptionResponse::from)
                .toList();
    }
}