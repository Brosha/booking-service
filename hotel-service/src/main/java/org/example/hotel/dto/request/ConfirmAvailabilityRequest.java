package org.example.hotel.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmAvailabilityRequest {

    private String bookingId;
    private LocalDate startDate;
    private LocalDate endDate;
}
