package com.railbit.dto;

/**
 * Workload and quality figures for one agent.
 *
 * @param assigned  every ticket currently assigned to the agent (pending + completed)
 * @param pending   ASSIGNED or IN_PROGRESS – work still to do
 * @param completed RESOLVED or CLOSED – work done
 */
public record AgentPerformance(
        Long agentId,
        String fullName,
        String username,
        String email,
        boolean enabled,
        Long departmentId,
        String departmentName,
        long assigned,
        long pending,
        long completed,
        Double averageRating,
        long ratingCount) {

    /** Share of assigned tickets that are completed, 0–100. */
    public int completionRate() {
        return assigned == 0 ? 0 : (int) Math.round(completed * 100.0 / assigned);
    }
}
