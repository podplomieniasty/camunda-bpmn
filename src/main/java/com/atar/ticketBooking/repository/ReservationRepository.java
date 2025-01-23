package com.atar.ticketBooking.repository;

import com.atar.ticketBooking.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByShowingIdAndSeatRowAndSeatCol(Long showingId, Integer seatRow, Integer seatCol);
}
