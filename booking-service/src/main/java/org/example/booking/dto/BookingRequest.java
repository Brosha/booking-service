package org.example.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequest {

    @NotNull
    private Long hotelId;

    @NotNull
    private Long roomId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}

