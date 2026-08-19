package com.spring.spotlog_api.domain.reservation;

import com.spring.spotlog_api.domain.member.Member;
import com.spring.spotlog_api.domain.member.MemberRepository;
import com.spring.spotlog_api.domain.member.MemberRole;
import com.spring.spotlog_api.domain.place.Place;
import com.spring.spotlog_api.domain.place.PlaceCategory;
import com.spring.spotlog_api.domain.place.PlaceRepository;
import com.spring.spotlog_api.domain.reservation.dto.ReservationCreateRequest;
import com.spring.spotlog_api.domain.reservation.service.PessimisticReservationService;
import com.spring.spotlog_api.domain.reservationoption.ReservationOption;
import com.spring.spotlog_api.domain.reservationoption.ReservationOptionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
class PessimisticReservationConcurrencyTest {
    @Autowired private PessimisticReservationService reservationService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private PlaceRepository placeRepository;
    @Autowired private ReservationOptionRepository optionRepository;
    @Autowired private ReservationRepository reservationRepository;

    private UUID optionId;
    private List<UUID> customerIds;

    @BeforeEach
    void setUp() {
        Member owner = memberRepository.save(
                Member.create("사장님", "owner@test.com", "encoded-password", MemberRole.OWNER)
        );

        Place place = placeRepository.save(
                Place.create(owner, PlaceCategory.CAFE, "테스트 카페", "설명",
                        "테스트 도로명주소", "테스트 지번주소", "12345",
                        37.0, 127.0, LocalTime.of(9, 0), LocalTime.of(22, 0))
        );

        ReservationOption option = optionRepository.save(
                ReservationOption.create(place, "동시성 테스트 옵션", "설명", 10, 60,
                        LocalTime.of(9, 0), LocalTime.of(22, 0))
        );
        optionId = option.getId();

        customerIds = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Member customer = memberRepository.save(
                    Member.create("고객" + i, "customer" + i + "@test.com", "encoded-password", MemberRole.CUSTOMER)
            );
            customerIds.add(customer.getId());
        }
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        optionRepository.deleteAll();
        placeRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void 같은_슬롯에_100명이_동시에_요청하면_1명만_성공한다() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        ReservationCreateRequest request = new ReservationCreateRequest(
                optionId, LocalDate.now().plusDays(1), LocalTime.of(13, 0), LocalTime.of(14, 0), 1
        );

        for (UUID customerId : customerIds) {
            executorService.submit(() -> {
                try {
                    reservationService.reserve(customerId, request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(99);
    }
}