package com.railbit.TicketManagementSystem.DTO;

public class TicketStatsDTO {
    private String day;        // e.g. 2025-08-14 (ISO) — easy for JS formatting
    private long open;
    private long inProgress;
    private long closed;
    private long assigned;
    private long unassigned;

    public TicketStatsDTO() {}

    public TicketStatsDTO(String day, long open, long inProgress, long closed, long assigned, long unassigned) {
        this.day = day;
        this.open = open;
        this.inProgress = inProgress;
        this.closed = closed;
        this.assigned = assigned;
        this.unassigned = unassigned;
    }

    // getters & setters
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
    public long getOpen() { return open; }
    public void setOpen(long open) { this.open = open; }
    public long getInProgress() { return inProgress; }
    public void setInProgress(long inProgress) { this.inProgress = inProgress; }
    public long getClosed() { return closed; }
    public void setClosed(long closed) { this.closed = closed; }
    public long getAssigned() { return assigned; }
    public void setAssigned(long assigned) { this.assigned = assigned; }
    public long getUnassigned() { return unassigned; }
    public void setUnassigned(long unassigned) { this.unassigned = unassigned; }
}
