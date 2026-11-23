package com.railbit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Audit trail entry: who did what to a ticket (created, assigned, resolved...). */
@Entity
@Table(name = "ticket_activities")
public class TicketActivity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    /** Null when the system performed the action. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(nullable = false, length = 500)
    private String description;

    protected TicketActivity() {
    }

    public TicketActivity(Ticket ticket, User actor, String description) {
        this.ticket = ticket;
        this.actor = actor;
        this.description = description;
    }

    public Ticket getTicket() { return ticket; }
    public User getActor() { return actor; }
    public String getDescription() { return description; }
}
