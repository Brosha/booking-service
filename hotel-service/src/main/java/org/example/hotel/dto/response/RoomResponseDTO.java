package org.example.hotel.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomResponseDTO {
    private Long id;
    private Long hotelId;
    private String number;
    private Boolean available;
    private Long timesBooked;
}
