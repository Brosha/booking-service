package org.example.hotel.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @Column(name = "number", nullable = false, unique = true)
    private String number;

    @Column(nullable = false)
    private Boolean available;

    @Column(nullable = false)
    private Long times_booked;

    @Column(name ="last_booking_id")
    private String lastBookingId;

    @Column(name = "hold_until")
    private OffsetDateTime holdUntil;


}
