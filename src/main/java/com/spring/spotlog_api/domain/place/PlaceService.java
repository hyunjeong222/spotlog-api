package com.spring.spotlog_api.domain.place;

import com.spring.spotlog_api.domain.member.Member;
import com.spring.spotlog_api.domain.member.MemberRepository;
import com.spring.spotlog_api.domain.place.dto.*;
import com.spring.spotlog_api.global.common.PageResponse;
import com.spring.spotlog_api.global.exception.CustomException;
import com.spring.spotlog_api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {
    private final PlaceRepository placeRepository;
    private final MemberRepository memberRepository;

    // 장소 등록 (OWNER만)
    @Transactional
    public PlaceCreateResponse create(String memberId, PlaceCreateRequest request) {
        Member member = memberRepository.findById(UUID.fromString(memberId))
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!member.isOwner()) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 같은 OWNER + 장소명 + 주소 중복 체크
        if (placeRepository.existsByOwnerAndNameAndRoadAddressAndDeletedFalse(
                member, request.name(), request.roadAddress())) {
            throw new CustomException(ErrorCode.DUPLICATE_PLACE);
        }

        Place place = Place.create(
                member,
                request.category(),
                request.name(),
                request.description(),
                request.roadAddress(),
                request.jibunAddress(),
                request.postalCode(),
                request.latitude(),
                request.longitude(),
                request.openTime(),
                request.closeTime()
        );

        placeRepository.save(place);
        return PlaceCreateResponse.from(place);
    }

    // 전체 장소 목록 조회 (카테고리 필터)
    public PageResponse<PlaceListResponse> getPlaces(
            PlaceCategory category, Pageable pageable) {
        Page<Place> page;
        if (category != null) {
            page = placeRepository.findByCategoryAndStatusAndDeletedFalse(
                    category, PlaceStatus.ACTIVE, pageable);
        } else {
            page = placeRepository.findByStatusAndDeletedFalse(
                    PlaceStatus.ACTIVE, pageable);
        }
        return PageResponse.from(page.map(PlaceListResponse::from));
    }

    // 장소 상세 조회
    public PlaceDetailResponse getPlace(UUID placeId) {
        Place place = placeRepository.findByIdAndDeletedFalse(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));
        return PlaceDetailResponse.from(place);
    }

    // OWNER 본인 장소 목록 조회
    public PageResponse<PlaceListResponse> getMyPlaces(
            String memberId, Pageable pageable) {
        Member member = memberRepository.findById(UUID.fromString(memberId))
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Page<Place> page = placeRepository.findByOwnerAndDeletedFalse(member, pageable);
        return PageResponse.from(page.map(PlaceListResponse::from));
    }

    // 장소 활성화
    @Transactional
    public void activate(String memberId, UUID placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        if (!place.isOwnedBy(UUID.fromString(memberId))) {
            throw new CustomException(ErrorCode.NO_PLACE_PERMISSION);
        }

        place.activate();
    }

    // 장소 비활성화
    @Transactional
    public void deactivate(String memberId, UUID placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        if (!place.isOwnedBy(UUID.fromString(memberId))) {
            throw new CustomException(ErrorCode.NO_PLACE_PERMISSION);
        }

        place.deactivate();
    }

    // 장소 수정
    @Transactional
    public PlaceDetailResponse update(String memberId, UUID placeId, PlaceUpdateRequest request) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        if (!place.isOwnedBy(UUID.fromString(memberId))) {
            throw new CustomException(ErrorCode.NO_PLACE_PERMISSION);
        }

        place.update(request);
        return PlaceDetailResponse.from(place);
    }

    // 장소 삭제
    @Transactional
    public void delete(String memberId, UUID placeId) {
        Place place = placeRepository.findByIdAndDeletedFalse(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        if (!place.isOwnedBy(UUID.fromString(memberId))) {
            throw new CustomException(ErrorCode.NO_PLACE_PERMISSION);
        }

        place.delete();
    }
}