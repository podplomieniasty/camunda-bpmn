package com.atar.ticketBooking.service;

import com.atar.ticketBooking.model.Showing;
import com.atar.ticketBooking.repository.ShowingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowingService {

    @Autowired
    private ShowingRepository showingRepository;

    public List<Showing> getAllDates() { return showingRepository.findAll(); }
    public List<Showing> getAllMovieShowings(Long movieId) {
        return showingRepository.findByMovieId(movieId);
    }
}
