package com.railbit.Entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
public class TicketRating {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(optional = false) @JoinColumn(name = "ticket_id", unique = true) private Ticket ticket;
    @Column(nullable = false) private int score;
    @Column(length = 1000) private String feedback;
    @Column(nullable = false) private LocalDateTime ratedAt;
    @PrePersist void created() { ratedAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
