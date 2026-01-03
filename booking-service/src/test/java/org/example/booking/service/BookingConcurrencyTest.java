package org.example.booking.service;

import org.example.booking.client.HotelClient;
import org.example.booking.dto.BookingRequest;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
class BookingConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private HotelClient hotelClient;

    @TestConfiguration
    static class BookingConcurrencyTestConfig {
        @Bean
        @Primary
        HotelClient hotelClientMock() {
            return Mockito.mock(HotelClient.class);
        }
    }

    @Test
    void concurrentBookings_onlyOneSucceedsForSameRoomAndDates() throws Exception {
        doNothing().when(hotelClient).confirmAvailability(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.any()
        );

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);

        int threads = 5;
        var executor = Executors.newFixedThreadPool(threads);
        List<Callable<Boolean>> tasks = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            final long userId = i + 1;
            tasks.add(() -> {
                BookingRequest request = new BookingRequest();
                request.setHotelId(1L);
                request.setRoomId(1L);
                request.setStartDate(start);
                request.setEndDate(end);
                try {
                    bookingService.createBooking(userId, request, null);
                    return true;
                } catch (IllegalStateException ex) {
                    return false;
                }
            });
        }

        List<Future<Boolean>> futures = executor.invokeAll(tasks);
        executor.shutdown();

        long successCount = futures.stream()
                .filter(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();

        assertEquals(1, successCount);
        assertEquals(1, bookingRepository.count());
        assertEquals(Status.CONFIRMED, bookingRepository.findAll().get(0).getStatus());
    }
}
