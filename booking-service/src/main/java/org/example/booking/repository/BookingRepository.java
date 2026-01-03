package org.example.booking.repository;

import jakarta.persistence.LockModeType;
import org.example.booking.entity.Booking;
import org.example.booking.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    java.util.Optional<Booking> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b " +
           "where b.roomId = :roomId " +
           "and b.status in :statuses " +
           "and b.startDate < :endDate " +
           "and b.endDate > :startDate")
    List<Booking> findOverlappingForUpdate(@Param("roomId") Long roomId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate,
                                           @Param("statuses") Collection<Status> statuses);

    @Query("select b from Booking b " +
           "where b.roomId = :roomId " +
           "and b.status in :statuses " +
           "and b.startDate < :endDate " +
           "and b.endDate > :startDate")
    List<Booking> findOverlapping(@Param("roomId") Long roomId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate,
                                  @Param("statuses") Collection<Status> statuses);
}
