package org.example.hotel.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomStatsResponse {

    private Long hotelId;
    private long totalRooms;
    private long availableRooms;
    private long totalTimesBooked;
}

