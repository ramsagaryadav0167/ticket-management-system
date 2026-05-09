package com.railbit.TicketManagementSystem.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private HistoryType type;

    @Column(nullable = false)
    private String description;

    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    // ✅ Auto-set timestamp before saving
    @PrePersist
    public void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    // ✅ Constructors

    public TicketHistory() {
    }

    public TicketHistory(HistoryType type, String description, Ticket ticket, User updatedBy) {
        this.type = type;
        this.description = description;
        this.ticket = ticket;
        this.updatedBy = updatedBy;
    }

    // ✅ Getters and Setters

    public Long getId() {
        return id;
    }

    public HistoryType getType() {
        return type;
    }

    public void setType(HistoryType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    // ✅ Optional: Useful for logging/debugging
    @Override
    public String toString() {
        return "TicketHistory{" +
                "id=" + id +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                ", ticket=" + (ticket != null ? ticket.getId() : null) +
                ", updatedBy=" + (updatedBy != null ? updatedBy.getFullName() : null) +
                '}';
    }
}
