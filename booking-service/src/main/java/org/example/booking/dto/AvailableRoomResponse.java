package org.example.booking.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AvailableRoomResponse {

    private Long id;
    private Long hotelId;
    private String number;
    private Long timesBooked;
}

