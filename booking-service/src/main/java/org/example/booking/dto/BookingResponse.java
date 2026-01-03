package org.example.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class BookingResponse {

    private Long id;
    private Long hotelId;
    private Long roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}

