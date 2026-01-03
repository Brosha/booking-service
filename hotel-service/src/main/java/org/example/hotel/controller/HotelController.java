package org.example.hotel.controller;

import lombok.RequiredArgsConstructor;

import org.example.hotel.dto.request.ConfirmAvailabilityRequest;
import org.example.hotel.dto.request.HotelDTORequest;
import org.example.hotel.dto.request.ReleaseRequest;
import org.example.hotel.dto.request.RoomDTORequest;
import org.example.hotel.dto.response.HotelResponseDTO;
import org.example.hotel.dto.response.RoomResponseDTO;
import org.example.hotel.dto.response.RoomStatsResponse;
import org.example.hotel.service.HotelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @PostMapping("/hotels")
    public ResponseEntity<HotelResponseDTO> createHotel(@RequestBody HotelDTORequest hotel) {
        return ResponseEntity.ok(hotelService.createHotel(hotel));
    }

    @PostMapping("/rooms")
    public ResponseEntity<RoomResponseDTO> createRoom(@RequestBody RoomDTORequest roomRequest) {
        return ResponseEntity.ok(hotelService.createRoom(roomRequest));
    }

    @GetMapping("/hotels")
    public ResponseEntity<List<HotelResponseDTO>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotels());
    }

    @GetMapping("/hotels/{id}")
    public ResponseEntity<HotelResponseDTO> getHotel(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<RoomResponseDTO>> getAllAvailableRooms() {
        return ResponseEntity.ok(hotelService.getAllAvailableRooms());
    }

    @GetMapping("/rooms/recommend")
    public ResponseEntity<List<RoomResponseDTO>> getRecommendedRooms() {
        return ResponseEntity.ok(hotelService.getRecommendedRooms());
    }

    @GetMapping("/hotels/stats/rooms")
    public ResponseEntity<RoomStatsResponse> getRoomStats(@RequestParam Long hotelId) {
        return ResponseEntity.ok(hotelService.getRoomStats(hotelId));
    }

    @PostMapping("/rooms/{id}/confirm-availability")
    public ResponseEntity<Void> confirmRoomAvailability(@PathVariable Long id,
                                                        @RequestBody ConfirmAvailabilityRequest request) {
        hotelService.confirmRoomAvailability(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rooms/{id}/release")
    public ResponseEntity<Void> releaseRoom(@PathVariable Long id,
                                            @RequestBody ReleaseRequest request) {
        hotelService.releaseRoom(id, request);
        return ResponseEntity.ok().build();
    }
}
