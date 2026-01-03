package org.example.hotel.dto.request;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class HotelDTORequest {
    private String name;
    private String address;
    private int rating;

    // Getters and Setters
}
