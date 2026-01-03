package org.example.hotel.dto.request;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RoomDTORequest {

    private Long hotelId;
    private String number;
    private Boolean available;
}

