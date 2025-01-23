package com.atar.ticketBooking.service;

import com.atar.ticketBooking.model.Showing;
import com.atar.ticketBooking.repository.ShowingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class ShowingService {

    @Autowired
    private ShowingRepository showingRepository;

    public List<Showing> getAllDates() { return showingRepository.findAll(); }
    public List<Showing> getAllMovieShowings(Long movieId) {
        return showingRepository.findByMovieId(movieId);
    }


    public Showing getShowingById(Long id) {
        return showingRepository.findById(id).orElse(null);
    }

    public boolean isShowingLessThan24HoursAway(Showing showing) {
        try {
            String dateTimeString = showing.getDate() + " " + showing.getHour(); // Format: "DD.MM.RRRR GG:MM"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            LocalDateTime showingDateTime = LocalDateTime.parse(dateTimeString, formatter);

            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(now, showingDateTime);

            return duration.toHours() < 24;
        } catch (DateTimeParseException e) {
            System.err.println("Failed to parse date or hour: " + showing);
            throw new IllegalArgumentException("Invalid date or hour format in showing: " + showing, e);
        }
    }

}
