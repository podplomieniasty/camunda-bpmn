package com.atar.ticketBooking.repository;

import com.atar.ticketBooking.model.CinemaRoom;
import com.atar.ticketBooking.model.Showing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CinemaRoomRepository extends JpaRepository<CinemaRoom, Long> {

}
