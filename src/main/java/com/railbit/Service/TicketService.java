package com.railbit.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.railbit.Entity.*;
import com.railbit.Repository.*;

@Service
@Transactional
public class TicketService {
    private static final List<String> ACTIVE_STATUSES = List.of("ASSIGNED");
    private final TicketRepository tickets;
    private final TicketMessageRepository messages;
    private final TicketRatingRepository ratings;
    private final UserService users;
    public TicketService(TicketRepository tickets, TicketMessageRepository messages, TicketRatingRepository ratings, UserService users) {
        this.tickets = tickets; this.messages = messages; this.ratings = ratings; this.users = users;
    }
    public Ticket create(User customer, String title, String description) {
        Ticket ticket = new Ticket(); ticket.setCustomer(customer); ticket.setTitle(title.trim()); ticket.setDescription(description.trim());
        return tickets.save(ticket);
    }
    public List<Ticket> customerTickets(User customer) { return tickets.findByCustomerOrderByCreatedAtDesc(customer); }
    public List<Ticket> agentTickets(User agent) { return tickets.findByAgentOrderByCreatedAtDesc(agent); }
    public List<Ticket> unassignedTickets() { return tickets.findByStatusOrderByCreatedAtAsc("OPEN"); }
    public Ticket get(Long id) { return tickets.findById(id).orElseThrow(() -> new IllegalArgumentException("Ticket not found")); }
    public Ticket assignLeastLoaded(Long id) {
        Ticket ticket = get(id);
        if (!"OPEN".equals(ticket.getStatus())) throw new IllegalStateException("Only open tickets can be assigned.");
        User agent = users.agents().stream().min(Comparator.comparingLong(a -> activeCount(a))).orElseThrow(() -> new IllegalStateException("Create an agent before assigning tickets."));
        ticket.setAgent(agent); ticket.setStatus("ASSIGNED"); ticket.setAssignedAt(LocalDateTime.now());
        return ticket;
    }
    public long activeCount(User agent) { return tickets.countByAgentAndStatusIn(agent, ACTIVE_STATUSES); }
    public long completedCount(User agent) { return tickets.countByAgentAndStatusIn(agent, List.of("COMPLETED", "CLOSED")); }
    public void complete(Long id, User agent) {
        Ticket ticket = get(id); requireAgent(ticket, agent);
        if (!"ASSIGNED".equals(ticket.getStatus())) throw new IllegalStateException("Only assigned tickets can be completed.");
        ticket.setStatus("COMPLETED"); ticket.setCompletedAt(LocalDateTime.now());
    }
    public List<TicketMessage> messages(Long id, User user) { Ticket ticket = get(id); requireParticipant(ticket, user); return messages.findByTicketOrderBySentAtAsc(ticket); }
    public void sendMessage(Long id, User sender, String body) {
        Ticket ticket = get(id); requireParticipant(ticket, sender);
        if ("CLOSED".equals(ticket.getStatus())) throw new IllegalStateException("This ticket is closed.");
        if (body == null || body.isBlank()) throw new IllegalArgumentException("Message cannot be blank.");
        TicketMessage message = new TicketMessage(); message.setTicket(ticket); message.setSender(sender); message.setBody(body.trim()); messages.save(message);
    }
    public boolean canRate(Ticket ticket, User customer) { return ticket.getCustomer().getId().equals(customer.getId()) && "COMPLETED".equals(ticket.getStatus()) && ratings.findByTicket(ticket).isEmpty(); }
    public void rate(Long id, User customer, int score, String feedback) {
        Ticket ticket = get(id);
        if (!canRate(ticket, customer)) throw new IllegalStateException("This ticket is not ready for a rating.");
        if (score < 1 || score > 5) throw new IllegalArgumentException("Rating must be between 1 and 5.");
        TicketRating rating = new TicketRating(); rating.setTicket(ticket); rating.setScore(score); rating.setFeedback(feedback == null ? "" : feedback.trim()); ratings.save(rating); ticket.setStatus("CLOSED");
    }
    private void requireAgent(Ticket ticket, User user) { if (ticket.getAgent() == null || !ticket.getAgent().getId().equals(user.getId())) throw new SecurityException("This ticket is not assigned to you."); }
    private void requireParticipant(Ticket ticket, User user) { if (ticket.getCustomer().getId().equals(user.getId())) return; requireAgent(ticket, user); }
}
