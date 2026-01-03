package org.example.booking.service;

import org.example.booking.client.HotelClient;
import org.example.booking.dto.BookingRequest;
import org.example.booking.dto.BookingResponse;
import org.example.booking.entity.Booking;
import org.example.booking.entity.Status;
import org.example.booking.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private HotelClient hotelClient;

    @TestConfiguration
    static class BookingServiceTestConfig {
        @Bean
        @Primary
        HotelClient hotelClientMock() {
            return Mockito.mock(HotelClient.class);
        }
    }

    @Test
    @Transactional
    void createBooking_successfullyCreatesConfirmedBooking() {
        doNothing().when(hotelClient).confirmAvailability(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.any()
        );

        BookingRequest request = new BookingRequest();
        request.setHotelId(1L);
        request.setRoomId(1L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));

        BookingResponse response = bookingService.createBooking(1L, request, null);

        assertNotNull(response.getId());
        assertEquals(Status.CONFIRMED.name(), response.getStatus());
        Booking saved = bookingRepository.findById(response.getId()).orElseThrow();
        assertEquals(Status.CONFIRMED, saved.getStatus());
    }

    @Test
    @Transactional
    void createBooking_conflictOnOverlappingDates() {
        doNothing().when(hotelClient).confirmAvailability(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.any()
        );

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);

        Booking existing = Booking.builder()
                .userId(1L)
                .hotelId(1L)
                .roomId(1L)
                .startDate(start)
                .endDate(end)
                .status(Status.CONFIRMED)
                .build();
        bookingRepository.save(existing);

        BookingRequest request = new BookingRequest();
        request.setHotelId(1L);
        request.setRoomId(1L);
        request.setStartDate(start.plusDays(0));
        request.setEndDate(end.minusDays(0));

        assertThrows(IllegalStateException.class,
                () -> bookingService.createBooking(2L, request, null));
    }

    @Test
    @Transactional
    void createBooking_isIdempotentForSameKey() {
        doNothing().when(hotelClient).confirmAvailability(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.any()
        );

        BookingRequest request = new BookingRequest();
        request.setHotelId(1L);
        request.setRoomId(1L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));

        String key = "idem-key-123";

        BookingResponse first = bookingService.createBooking(1L, request, key);
        BookingResponse second = bookingService.createBooking(1L, request, key);

        assertEquals(first.getId(), second.getId());
        Booking saved = bookingRepository.findById(first.getId()).orElseThrow();
        assertEquals(Status.CONFIRMED, saved.getStatus());
    }

    @Test
    @Transactional
    void createBooking_compensatesWhenHotelConfirmFails() {
        doThrow(new RuntimeException("hotel down"))
                .when(hotelClient)
                .confirmAvailability(ArgumentMatchers.anyLong(), ArgumentMatchers.any());

        BookingRequest request = new BookingRequest();
        request.setHotelId(1L);
        request.setRoomId(1L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> bookingService.createBooking(1L, request, null));
        assertTrue(ex.getMessage().contains("Failed to confirm room availability"));

        // Должна быть создана запись с CANCELLED (саговая компенсация)
        assertEquals(1L, bookingRepository.count());
        Booking saved = bookingRepository.findAll().get(0);
        assertEquals(Status.CANCELLED, saved.getStatus());
    }

    @Test
    @Transactional
    void getBooking_accessDeniedForOtherUser() {
        doNothing().when(hotelClient).confirmAvailability(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.any()
        );

        BookingRequest request = new BookingRequest();
        request.setHotelId(1L);
        request.setRoomId(1L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));

        BookingResponse created = bookingService.createBooking(1L, request, null);

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> bookingService.getBooking(2L, created.getId()));
    }

    @Test
    @Transactional
    void getUserBookings_supportsPagination() {
        doNothing().when(hotelClient).confirmAvailability(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.any()
        );

        for (int i = 0; i < 3; i++) {
            BookingRequest request = new BookingRequest();
            request.setHotelId(1L);
            request.setRoomId(1L);
            request.setStartDate(LocalDate.now().plusDays(1 + i * 3L));
            request.setEndDate(LocalDate.now().plusDays(3 + i * 3L));
            bookingService.createBooking(1L, request, "page-key-" + i);
        }

        var firstPage = bookingService.getUserBookings(1L, 0, 2);
        var secondPage = bookingService.getUserBookings(1L, 1, 2);

        assertEquals(2, firstPage.size());
        assertEquals(1, secondPage.size());
    }
}
