package com.railbit.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.railbit.Entity.Ticket;
import com.railbit.Entity.TicketRating;

public interface TicketRatingRepository extends JpaRepository<TicketRating, Long> {
    Optional<TicketRating> findByTicket(Ticket ticket);
}
