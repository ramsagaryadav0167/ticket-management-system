package com.railbit.TicketManagementSystem.Repository;

import com.railbit.TicketManagementSystem.Entity.TicketHistory;
import com.railbit.TicketManagementSystem.Entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {
    List<TicketHistory> findByTicketOrderByTimestampDesc(Ticket ticket);
    List<TicketHistory> findByTicketIdOrderByTimestampDesc(Long ticketId);
    @Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.historyList WHERE t.id = :id")
    Optional<Ticket> findByIdWithHistory(@Param("id") Long id);
    List<TicketHistory> findByTicketId(Long ticketId);
    
}
