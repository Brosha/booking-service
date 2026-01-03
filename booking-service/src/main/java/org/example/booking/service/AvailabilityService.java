package org.example.booking.service;

import lombok.RequiredArgsConstructor;
import org.example.booking.client.HotelClient;
import org.example.booking.client.dto.RoomSummary;
import org.example.booking.dto.AvailableRoomResponse;
import org.example.booking.entity.Booking;
import org.example.booking.entity.Status;
import org.example.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final HotelClient hotelClient;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public List<AvailableRoomResponse> getAvailableRooms(LocalDate startDate,
                                                         LocalDate endDate,
                                                         Long hotelId,
                                                         Integer limit,
                                                         boolean recommend) {
        if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
            throw new IllegalArgumentException("Invalid date range");
        }

        List<RoomSummary> rooms = recommend
                ? hotelClient.getRecommendedRooms()
                : hotelClient.getAllRooms();

        if (hotelId != null) {
            rooms = rooms.stream()
                    .filter(r -> hotelId.equals(r.getHotelId()))
                    .collect(Collectors.toList());
        }

        List<AvailableRoomResponse> result = rooms.stream()
                .filter(RoomSummary::getAvailable)
                .filter(room -> isRoomFree(room.getId(), startDate, endDate))
                .map(this::toResponse)
                .sorted(recommend
                        ? Comparator.comparing(AvailableRoomResponse::getTimesBooked)
                            .thenComparing(AvailableRoomResponse::getId)
                        : Comparator.comparing(AvailableRoomResponse::getId))
                .collect(Collectors.toList());

        if (limit != null && limit > 0 && result.size() > limit) {
            return result.subList(0, limit);
        }
        return result;
    }

    private boolean isRoomFree(Long roomId, LocalDate startDate, LocalDate endDate) {
        List<Booking> overlapping = bookingRepository.findOverlapping(
                roomId,
                startDate,
                endDate,
                Arrays.asList(Status.PENDING, Status.CONFIRMED)
        );
        return overlapping.isEmpty();
    }

    private AvailableRoomResponse toResponse(RoomSummary room) {
        return AvailableRoomResponse.builder()
                .id(room.getId())
                .hotelId(room.getHotelId())
                .number(room.getNumber())
                .timesBooked(room.getTimesBooked())
                .build();
    }
}
