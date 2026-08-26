package com.railbit.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.railbit.Entity.Ticket;
import com.railbit.Entity.TicketMessage;

public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {
    List<TicketMessage> findByTicketOrderBySentAtAsc(Ticket ticket);
}
