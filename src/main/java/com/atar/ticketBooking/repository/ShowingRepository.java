package com.atar.ticketBooking.repository;

import com.atar.ticketBooking.model.Showing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShowingRepository extends JpaRepository<Showing, Long> {
    List<Showing> findByMovieId(Long movieId);
}
