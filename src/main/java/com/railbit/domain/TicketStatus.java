package com.railbit.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Ticket lifecycle:
 * OPEN (in department queue, no agent) -> ASSIGNED -> IN_PROGRESS -> RESOLVED -> CLOSED.
 * A resolved ticket can be reopened by the customer (back to IN_PROGRESS).
 */
public enum TicketStatus {
    OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED;

    /** Work an agent still has to do. */
    public static final Set<TicketStatus> PENDING = EnumSet.of(ASSIGNED, IN_PROGRESS);
    /** Work an agent has finished. */
    public static final Set<TicketStatus> COMPLETED = EnumSet.of(RESOLVED, CLOSED);

    public boolean isPending() {
        return PENDING.contains(this);
    }

    public boolean isCompleted() {
        return COMPLETED.contains(this);
    }

    public String label() {
        return name().replace('_', ' ');
    }
}
