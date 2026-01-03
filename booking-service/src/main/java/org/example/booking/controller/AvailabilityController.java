package org.example.booking.controller;

import lombok.RequiredArgsConstructor;
import org.example.booking.dto.AvailableRoomResponse;
import org.example.booking.service.AvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/rooms")
    public ResponseEntity<List<AvailableRoomResponse>> getAvailableRooms(@AuthenticationPrincipal Jwt jwt,
                                                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                                         @RequestParam(required = false) Long hotelId,
                                                                         @RequestParam(required = false) Integer limit) {
        Long ignoredUserId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(
                availabilityService.getAvailableRooms(startDate, endDate, hotelId, limit, false)
        );
    }

    @GetMapping("/recommend")
    public ResponseEntity<List<AvailableRoomResponse>> getRecommendedRooms(@AuthenticationPrincipal Jwt jwt,
                                                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                                           @RequestParam(required = false) Long hotelId,
                                                                           @RequestParam(required = false) Integer limit) {
        Long ignoredUserId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(
                availabilityService.getAvailableRooms(startDate, endDate, hotelId, limit, true)
        );
    }
}

