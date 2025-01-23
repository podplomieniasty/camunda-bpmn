package com.atar.ticketBooking.repository;

import com.atar.ticketBooking.model.Reservation;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByShowingIdAndSeatRowAndSeatCol(Long showingId, Integer seatRow, Integer seatCol);

    boolean existsByAccessCodeAndEmail(String accessCode, String email);

    @Query("SELECT r.showingId FROM Reservation r WHERE r.accessCode = :accessCode")
    Long findShowingIdByAccessCode(@Param("accessCode") String accessCode);

    @Modifying
    @Transactional
    @Query("DELETE FROM Reservation r WHERE r.accessCode = :accessCode")
    int deleteByAccessCode(@Param("accessCode") String accessCode);
}
