package com.railbit.dto;

public record DashboardStats(
        long total,
        long queued,
        long pending,
        long completed,
        long agents,
        long departments) {
}
