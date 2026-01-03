package org.example.booking.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.example.booking.client.dto.ConfirmAvailabilityCommand;
import org.example.booking.client.dto.ReleaseCommand;
import org.example.booking.client.dto.RoomSummary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HotelClient {

    private final RestTemplate restTemplate;

    @Retry(name = "hotelConfirm")
    @CircuitBreaker(name = "hotelConfirm")
    public void confirmAvailability(Long roomId, ConfirmAvailabilityCommand command) {
        String url = "http://hotel-service/api/rooms/" + roomId + "/confirm-availability";
        ResponseEntity<Void> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(command),
                Void.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Hotel confirm-availability failed with status " + response.getStatusCode());
        }
    }

    @Retry(name = "hotelRelease")
    @CircuitBreaker(name = "hotelRelease")
    public void release(Long roomId, ReleaseCommand command) {
        String url = "http://hotel-service/api/rooms/" + roomId + "/release";
        ResponseEntity<Void> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(command),
                Void.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Hotel release failed with status " + response.getStatusCode());
        }
    }

    @Retry(name = "hotelRooms")
    @CircuitBreaker(name = "hotelRooms")
    public List<RoomSummary> getAllRooms() {
        String url = "http://hotel-service/api/rooms";
        ResponseEntity<List<RoomSummary>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RoomSummary>>() {
                }
        );
        return response.getBody();
    }

    @Retry(name = "hotelRecommend")
    @CircuitBreaker(name = "hotelRecommend")
    public List<RoomSummary> getRecommendedRooms() {
        String url = "http://hotel-service/api/rooms/recommend";
        ResponseEntity<List<RoomSummary>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RoomSummary>>() {
                }
        );
        return response.getBody();
    }
}
