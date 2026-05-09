package com.railbit.TicketManagementSystem.Service;

import com.railbit.TicketManagementSystem.Entity.*;
import com.railbit.TicketManagementSystem.Repository.TicketHistoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketHistoryService {

    @Autowired
    private TicketHistoryRepository ticketHistoryRepository;

    /**
     * Logs a new ticket history event.
     * 
     * @param type        Type of history event
     * @param description Description of what changed
     * @param ticket      The ticket related to the event
     * @param updatedBy   User who made the change (can be null for system events)
     */
    public void log(HistoryType type, String description, Ticket ticket, User updatedBy) {
        if (type == null || description == null || ticket == null) {
            throw new IllegalArgumentException("HistoryType, description, and ticket cannot be null.");
        }

        TicketHistory history = new TicketHistory(type, description, ticket, updatedBy);
        ticketHistoryRepository.save(history);
    }

    /**
     * Overloaded log method for system-generated logs with no user.
     */
    public void log(HistoryType type, String description, Ticket ticket) {
        log(type, description, ticket, null);
    }

    /**
     * Fetch ticket history sorted by latest timestamp.
     */
    public List<TicketHistory> getHistoryForTicket(Ticket ticket) {
        if (ticket == null) return List.of();
        return ticketHistoryRepository.findByTicketOrderByTimestampDesc(ticket);
    }

    /**
     * Fetch ticket history using ticket ID.
     */
    public List<TicketHistory> getHistoryByTicketId(Long ticketId) {
        if (ticketId == null) return List.of();
        return ticketHistoryRepository.findByTicketId(ticketId);
    }
}
