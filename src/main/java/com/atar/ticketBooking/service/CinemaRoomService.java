package com.atar.ticketBooking.service;

import com.atar.ticketBooking.model.CinemaRoom;
import com.atar.ticketBooking.repository.CinemaRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CinemaRoomService {

    @Autowired
    private CinemaRoomRepository cinemaRoomRepository;

    public List<CinemaRoom> getAll() { return cinemaRoomRepository.findAll(); }
    public Optional<CinemaRoom> findById(Long id) { return cinemaRoomRepository.findById(id); }
}
