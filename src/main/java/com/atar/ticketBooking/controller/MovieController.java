package com.atar.ticketBooking.controller;

import com.atar.ticketBooking.model.Movie;
import com.atar.ticketBooking.service.MovieService;
import com.sun.mail.iap.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @GetMapping("/movies")
    public ResponseEntity<List<Movie>> getMovies() {

        var movies = movieService.getAllMovies();
        return ResponseEntity.ok(movies);
    }

//    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter subscribe(@RequestParam String processInstanceKey) {
//        return emitterService.addListener(processInstanceKey);
//    }



}
