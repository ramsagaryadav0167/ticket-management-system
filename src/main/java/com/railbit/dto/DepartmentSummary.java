package com.railbit.dto;

import com.railbit.domain.Department;

/**
 * A department with its team size and ticket figures.
 *
 * @param queued tickets waiting in the department queue (status OPEN, no agent yet)
 */
public record DepartmentSummary(
        Department department,
        long agentCount,
        long activeAgentCount,
        long queued,
        long pending,
        long completed) {

    public long total() {
        return queued + pending + completed;
    }
}
