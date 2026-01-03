package org.example.hotel.service;

import org.example.hotel.dto.request.ConfirmAvailabilityRequest;
import org.example.hotel.dto.request.ReleaseRequest;
import org.example.hotel.entity.Room;
import org.example.hotel.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HotelServiceTest {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    @Transactional
    void confirmRoomAvailability_setsHoldAndIncrementsCounter() {
        Room roomBefore = roomRepository.findById(1L).orElseThrow();
        long initialTimesBooked = roomBefore.getTimes_booked();

        ConfirmAvailabilityRequest request = ConfirmAvailabilityRequest.builder()
                .bookingId("booking-1")
                .startDate(OffsetDateTime.now().toLocalDate())
                .endDate(OffsetDateTime.now().plusDays(1).toLocalDate())
                .build();

        hotelService.confirmRoomAvailability(1L, request);

        Room roomAfter = roomRepository.findById(1L).orElseThrow();
        assertEquals("booking-1", roomAfter.getLastBookingId());
        assertNotNull(roomAfter.getHoldUntil());
        assertTrue(roomAfter.getHoldUntil().isAfter(OffsetDateTime.now().minusMinutes(1)));
        assertEquals(initialTimesBooked + 1, roomAfter.getTimes_booked());
    }

    @Test
    @Transactional
    void confirmRoomAvailability_rejectsWhenHeldByAnotherBooking() {
        ConfirmAvailabilityRequest first = ConfirmAvailabilityRequest.builder()
                .bookingId("booking-1")
                .startDate(OffsetDateTime.now().toLocalDate())
                .endDate(OffsetDateTime.now().plusDays(1).toLocalDate())
                .build();
        hotelService.confirmRoomAvailability(2L, first);

        ConfirmAvailabilityRequest second = ConfirmAvailabilityRequest.builder()
                .bookingId("booking-2")
                .startDate(OffsetDateTime.now().toLocalDate())
                .endDate(OffsetDateTime.now().plusDays(1).toLocalDate())
                .build();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> hotelService.confirmRoomAvailability(2L, second));
        assertTrue(ex.getMessage().contains("temporarily held"));
    }

    @Test
    @Transactional
    void releaseRoom_clearsHoldForSameBooking() {
        ConfirmAvailabilityRequest confirm = ConfirmAvailabilityRequest.builder()
                .bookingId("booking-3")
                .startDate(OffsetDateTime.now().toLocalDate())
                .endDate(OffsetDateTime.now().plusDays(1).toLocalDate())
                .build();
        hotelService.confirmRoomAvailability(4L, confirm);

        ReleaseRequest release = ReleaseRequest.builder()
                .bookingId("booking-3")
                .build();
        hotelService.releaseRoom(4L, release);

        Room room = roomRepository.findById(4L).orElseThrow();
        assertNull(room.getHoldUntil());
        assertNull(room.getLastBookingId());
    }

    @Test
    @Transactional
    void releaseRoom_rejectsDifferentBooking() {
        ConfirmAvailabilityRequest confirm = ConfirmAvailabilityRequest.builder()
                .bookingId("booking-4")
                .startDate(OffsetDateTime.now().toLocalDate())
                .endDate(OffsetDateTime.now().plusDays(1).toLocalDate())
                .build();
        hotelService.confirmRoomAvailability(5L, confirm);

        ReleaseRequest release = ReleaseRequest.builder()
                .bookingId("other-booking")
                .build();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> hotelService.releaseRoom(5L, release));
        assertTrue(ex.getMessage().contains("held by another booking"));
    }
}

