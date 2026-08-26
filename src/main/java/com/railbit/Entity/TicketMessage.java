package com.railbit.Entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
public class TicketMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Ticket ticket;
    @ManyToOne(optional = false) private User sender;
    @Column(nullable = false, length = 3000) private String body;
    @Column(nullable = false) private LocalDateTime sentAt;
    @PrePersist void created() { sentAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public LocalDateTime getSentAt() { return sentAt; }
}
