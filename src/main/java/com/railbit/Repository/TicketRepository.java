package com.railbit.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.railbit.Entity.Ticket;
import com.railbit.Entity.User;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCustomerOrderByCreatedAtDesc(User customer);
    List<Ticket> findByAgentOrderByCreatedAtDesc(User agent);
    List<Ticket> findByStatusOrderByCreatedAtAsc(String status);
    long countByAgentAndStatusIn(User agent, List<String> statuses);
    long countByAgentAndStatus(User agent, String status);
    long countByStatus(String status);
}
