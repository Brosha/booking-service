package org.example.hotel.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HotelResponseDTO {
    private Long id;
    private String name;
    private String address;
}
