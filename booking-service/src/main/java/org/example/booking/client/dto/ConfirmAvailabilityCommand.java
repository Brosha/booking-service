package org.example.booking.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ConfirmAvailabilityCommand {

    private String bookingId;
    private LocalDate startDate;
    private LocalDate endDate;
}

