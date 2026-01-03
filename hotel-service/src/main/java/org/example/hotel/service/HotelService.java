package org.example.hotel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hotel.dto.request.ConfirmAvailabilityRequest;
import org.example.hotel.dto.request.HotelDTORequest;
import org.example.hotel.dto.request.ReleaseRequest;
import org.example.hotel.dto.request.RoomDTORequest;
import org.example.hotel.dto.response.HotelResponseDTO;
import org.example.hotel.dto.response.RoomResponseDTO;
import org.example.hotel.dto.response.RoomStatsResponse;
import org.example.hotel.entity.Hotel;
import org.example.hotel.entity.Room;
import org.example.hotel.repository.HotelRepository;
import org.example.hotel.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public HotelResponseDTO createHotel(HotelDTORequest request) {
        Hotel hotel = Hotel.builder()
                .name(request.getName())
                .address(request.getAddress())
                .build();
        return convertToDTO(hotelRepository.save(hotel));
    }

    @Transactional
    public RoomResponseDTO createRoom(RoomDTORequest request) {
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
        Room room = Room.builder()
                .hotel(hotel)
                .number(request.getNumber())
                .available(request.getAvailable())
                .times_booked(0L)
                .build();
        return convertRoomToDTO(roomRepository.save(room));
    }

    @Transactional(readOnly = true)
    public List<HotelResponseDTO> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HotelResponseDTO getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
        return convertToDTO(hotel);
    }

    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getAllAvailableRooms() {
        return roomRepository.findByAvailableTrue().stream()
                .map(this::convertRoomToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getRecommendedRooms() {
        return roomRepository.findRecommendedRooms().stream()
                .map(this::convertRoomToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void confirmRoomAvailability(Long roomId, ConfirmAvailabilityRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!Boolean.TRUE.equals(room.getAvailable())) {
            throw new RuntimeException("Room is not available for booking");
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (room.getHoldUntil() != null
                && room.getHoldUntil().isAfter(now)
                && room.getLastBookingId() != null
                && !request.getBookingId().equals(room.getLastBookingId())) {
            throw new RuntimeException("Room is temporarily held by another booking");
        }

        room.setLastBookingId(request.getBookingId());
        room.setHoldUntil(now.plusMinutes(5));
        room.setTimes_booked(room.getTimes_booked() + 1);
        roomRepository.save(room);
    }

    @Transactional
    public void releaseRoom(Long roomId, ReleaseRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.getLastBookingId() != null
                && !room.getLastBookingId().equals(request.getBookingId())) {
            throw new RuntimeException("Room held by another booking");
        }

        room.setHoldUntil(null);
        room.setLastBookingId(null);
        roomRepository.save(room);
    }

    @Transactional(readOnly = true)
    public RoomStatsResponse getRoomStats(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
        List<Room> rooms = roomRepository.findByHotelId(hotel.getId());
        long totalRooms = rooms.size();
        long availableRooms = rooms.stream()
                .filter(r -> Boolean.TRUE.equals(r.getAvailable()))
                .count();
        long totalTimesBooked = rooms.stream()
                .mapToLong(Room::getTimes_booked)
                .sum();
        return RoomStatsResponse.builder()
                .hotelId(hotel.getId())
                .totalRooms(totalRooms)
                .availableRooms(availableRooms)
                .totalTimesBooked(totalTimesBooked)
                .build();
    }

    private HotelResponseDTO convertToDTO(Hotel hotel) {
        return HotelResponseDTO.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .address(hotel.getAddress())
                .build();
    }

    private RoomResponseDTO convertRoomToDTO(Room room) {
        return RoomResponseDTO.builder()
                .id(room.getId())
                .hotelId(room.getHotel().getId())
                .number(room.getNumber())
                .available(room.getAvailable())
                .timesBooked(room.getTimes_booked())
                .build();
    }
}
