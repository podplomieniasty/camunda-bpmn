package com.atar.ticketBooking.service;

import com.atar.ticketBooking.model.Reservation;
import com.atar.ticketBooking.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    public List<Reservation> getAllReservations() {return reservationRepository.findAll();}

    public Long addNewReservation(Reservation reservation) {
        reservationRepository.save(reservation);
        return reservation.getId();
    }

    public boolean isReservationAvailable(Long showingId, Integer row, Integer column) {
        var result = reservationRepository.findByShowingIdAndSeatRowAndSeatCol(showingId, row, column);
        if(!result.isEmpty()) {
            return false;
        }
        return true;
    }



    public boolean doesAccessCodeBelongToEmail(String accessCode, String email) {
        return reservationRepository.existsByAccessCodeAndEmail(accessCode, email);
    }

    public Long getShowingIdByAccessCode(String accessCode) {
        return reservationRepository.findShowingIdByAccessCode(accessCode);
    }

    public boolean cancelReservationByAccessCode(String accessCode) {
        int deletedRows = reservationRepository.deleteByAccessCode(accessCode);
        return deletedRows > 0; // Jeśli usunięto przynajmniej jeden rekord, zwróć true
    }
}
