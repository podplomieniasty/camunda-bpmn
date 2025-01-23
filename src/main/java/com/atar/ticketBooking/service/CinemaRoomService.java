package com.atar.ticketBooking.service;

import com.atar.ticketBooking.repository.CinemaRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CinemaRoomService {

    @Autowired
    private CinemaRoomRepository cinemaRoomRepository;
}
