package org.example.booking.client.dto;

import lombok.Data;

@Data
public class RoomSummary {

    private Long id;
    private Long hotelId;
    private String number;
    private Boolean available;
    private Long timesBooked;
}

