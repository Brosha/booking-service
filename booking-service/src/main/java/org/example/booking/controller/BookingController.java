package org.example.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.booking.dto.BookingRequest;
import org.example.booking.dto.BookingResponse;
import org.example.booking.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@AuthenticationPrincipal Jwt jwt,
                                                         @Valid @RequestBody BookingRequest request,
                                                         @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(bookingService.createBooking(userId, request, idempotencyKey));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getUserBookings(@AuthenticationPrincipal Jwt jwt,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(bookingService.getUserBookings(userId, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(@AuthenticationPrincipal Jwt jwt,
                                                      @PathVariable Long id) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(bookingService.getBooking(userId, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@AuthenticationPrincipal Jwt jwt,
                                              @PathVariable Long id) {
        Long userId = Long.valueOf(jwt.getSubject());
        bookingService.cancelBooking(userId, id);
        return ResponseEntity.ok().build();
    }
}
