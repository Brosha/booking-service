package org.example.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booking.client.HotelClient;
import org.example.booking.client.dto.ConfirmAvailabilityCommand;
import org.example.booking.client.dto.ReleaseCommand;
import org.example.booking.dto.BookingRequest;
import org.example.booking.dto.BookingResponse;
import org.example.booking.entity.Booking;
import org.example.booking.entity.Status;
import org.example.booking.repository.BookingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final HotelClient hotelClient;

    @Transactional
    public BookingResponse createBooking(Long userId, BookingRequest request, String idempotencyKey) {
        if (request.getStartDate().isAfter(request.getEndDate())
                || request.getStartDate().isEqual(request.getEndDate())) {
            throw new IllegalArgumentException("Invalid date range");
        }

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Booking existing = bookingRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey)
                    .orElse(null);
            if (existing != null) {
                return toResponse(existing);
            }
        }

        List<Booking> overlapping = bookingRepository.findOverlappingForUpdate(
                request.getRoomId(),
                request.getStartDate(),
                request.getEndDate(),
                Arrays.asList(Status.PENDING, Status.CONFIRMED)
        );
        if (!overlapping.isEmpty()) {
            throw new IllegalStateException("Room is already booked for given dates");
        }

        Booking booking = Booking.builder()
                .userId(userId)
                .hotelId(request.getHotelId())
                .roomId(request.getRoomId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(Status.PENDING)
                .idempotencyKey(idempotencyKey)
                .build();

        Booking saved = bookingRepository.save(booking);

        try {
            ConfirmAvailabilityCommand command = new ConfirmAvailabilityCommand(
                    String.valueOf(saved.getId()),
                    saved.getStartDate(),
                    saved.getEndDate()
            );
            hotelClient.confirmAvailability(saved.getRoomId(), command);
            saved.setStatus(Status.CONFIRMED);
            saved = bookingRepository.save(saved);
        } catch (RuntimeException ex) {
            log.warn("Hotel confirm-availability failed for booking {}: {}", saved.getId(), ex.getMessage());
            saved.setStatus(Status.CANCELLED);
            bookingRepository.save(saved);
            throw new IllegalStateException("Failed to confirm room availability", ex);
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(Long userId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));
        Page<Booking> bookingsPage = bookingRepository.findByUserId(userId, pageable);
        return bookingsPage.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getUserId().equals(userId)) {
            throw new AccessDeniedException("Access denied");
        }
        return toResponse(booking);
    }

    @Transactional
    public void cancelBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getUserId().equals(userId)) {
            throw new AccessDeniedException("Access denied");
        }
        booking.setStatus(Status.CANCELLED);
        bookingRepository.save(booking);

        try {
            ReleaseCommand command = new ReleaseCommand(String.valueOf(bookingId));
            hotelClient.release(booking.getRoomId(), command);
        } catch (RuntimeException ex) {
            log.warn("Hotel release failed for booking {}: {}", bookingId, ex.getMessage());
        }
    }

    private BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .hotelId(booking.getHotelId())
                .roomId(booking.getRoomId())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .status(booking.getStatus().name())
                .build();
    }
}
