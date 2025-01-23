package com.atar.ticketBooking.controller;

import com.atar.ticketBooking.model.Showing;
import com.atar.ticketBooking.service.ShowingService;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ShowingController {

    @Autowired
    private ShowingService showingService;

    @GetMapping("/showings")
    private ResponseEntity<List<Showing>> getAllShowings() {
        var result = showingService.getAllDates();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/showing")
    private ResponseEntity<List<Showing>> getMovieShowings(@RequestParam Long movieId) {
        var result = showingService.
    }
}
