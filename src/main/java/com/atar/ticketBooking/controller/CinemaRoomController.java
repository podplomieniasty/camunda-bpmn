package com.atar.ticketBooking.controller;

import com.atar.ticketBooking.model.CinemaRoom;
import com.atar.ticketBooking.service.CinemaRoomService;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class CinemaRoomController {

    @Autowired
    private CinemaRoomService cinemaRoomService;

    @GetMapping("/rooms")
    private ResponseEntity<List<CinemaRoom>> getAllRooms() {
        var result = cinemaRoomService.getAll();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/room")
    private ResponseEntity<Optional<CinemaRoom>> getRoom(@RequestParam(name = "room") Long id) {
        var result = cinemaRoomService.findById(id);
        return ResponseEntity.ok(result);
    }
}
