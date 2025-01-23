package com.atar.ticketBooking.controller;

import com.atar.ticketBooking.model.Reservation;
import com.atar.ticketBooking.service.ReservationService;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping("/reservations")
    private ResponseEntity<List<Reservation>> getAllReservations() {
        var result = reservationService.getAllReservations();
        return ResponseEntity.ok(result);
    }
}
