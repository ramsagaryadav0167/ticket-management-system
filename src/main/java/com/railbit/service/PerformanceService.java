package com.railbit.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.railbit.domain.Department;
import com.railbit.domain.Role;
import com.railbit.domain.TicketStatus;
import com.railbit.domain.User;
import com.railbit.dto.AgentPerformance;
import com.railbit.dto.DashboardStats;
import com.railbit.dto.DepartmentSummary;
import com.railbit.repository.DepartmentRepository;
import com.railbit.repository.TicketRatingRepository;
import com.railbit.repository.TicketRatingRepository.AgentRating;
import com.railbit.repository.TicketRepository;
import com.railbit.repository.TicketRepository.StatusCount;
import com.railbit.repository.UserRepository;

/**
 * Read-only reporting. Each method runs a fixed number of grouped queries regardless of how many
 * agents or departments exist, so the screens stay fast as the team grows.
 */
@Service
@Transactional(readOnly = true)
public class PerformanceService {

    private final TicketRepository tickets;
    private final TicketRatingRepository ratings;
    private final UserRepository users;
    private final DepartmentRepository departments;

    public PerformanceService(TicketRepository tickets, TicketRatingRepository ratings,
                              UserRepository users, DepartmentRepository departments) {
        this.tickets = tickets;
        this.ratings = ratings;
        this.users = users;
        this.departments = departments;
    }

    /** Performance of every agent, busiest first. */
    public List<AgentPerformance> allAgents() {
        return forAgents(users.findByRoleOrderByFullNameAsc(Role.AGENT));
    }

    public List<AgentPerformance> forDepartment(Long departmentId) {
        return forAgents(users.findByRoleAndDepartmentIdOrderByFullNameAsc(Role.AGENT, departmentId));
    }

    public AgentPerformance forAgent(User agent) {
        return forAgents(List.of(agent)).get(0);
    }

    public List<AgentPerformance> forAgents(List<User> agents) {
        Map<Long, Counts> counts = group(tickets.countGroupedByAgentAndStatus());
        Map<Long, AgentRating> ratingByAgent = ratings.averageByAgent().stream()
                .collect(Collectors.toMap(AgentRating::getAgentId, Function.identity()));

        return agents.stream()
                .map(agent -> {
                    Counts c = counts.getOrDefault(agent.getId(), Counts.EMPTY);
                    AgentRating r = ratingByAgent.get(agent.getId());
                    Department d = agent.getDepartment();
                    return new AgentPerformance(
                            agent.getId(), agent.getFullName(), agent.getUsername(), agent.getEmail(),
                            agent.isEnabled(),
                            d == null ? null : d.getId(), d == null ? null : d.getName(),
                            c.pending + c.completed, c.pending, c.completed,
                            r == null ? null : r.getAverage(), r == null ? 0 : r.getTotal());
                })
                .sorted(Comparator.comparingLong(AgentPerformance::pending).reversed()
                        .thenComparing(AgentPerformance::fullName))
                .toList();
    }

    public List<DepartmentSummary> departmentSummaries() {
        Map<Long, Counts> counts = group(tickets.countGroupedByDepartmentAndStatus());
        Map<Long, long[]> team = new HashMap<>();
        for (User agent : users.findByRoleOrderByFullNameAsc(Role.AGENT)) {
            if (agent.getDepartment() == null) continue;
            long[] t = team.computeIfAbsent(agent.getDepartment().getId(), k -> new long[2]);
            t[0]++;
            if (agent.isEnabled()) t[1]++;
        }
        return departments.findAllByOrderByNameAsc().stream()
                .map(d -> {
                    Counts c = counts.getOrDefault(d.getId(), Counts.EMPTY);
                    long[] t = team.getOrDefault(d.getId(), new long[2]);
                    return new DepartmentSummary(d, t[0], t[1], c.queued, c.pending, c.completed);
                })
                .toList();
    }

    public DepartmentSummary departmentSummary(Long departmentId) {
        return departmentSummaries().stream()
                .filter(s -> s.department().getId().equals(departmentId))
                .findFirst()
                .orElseThrow();
    }

    public DashboardStats dashboard() {
        long queued = tickets.countByStatus(TicketStatus.OPEN);
        long pending = tickets.countByStatus(TicketStatus.ASSIGNED) + tickets.countByStatus(TicketStatus.IN_PROGRESS);
        long completed = tickets.countByStatus(TicketStatus.RESOLVED) + tickets.countByStatus(TicketStatus.CLOSED);
        return new DashboardStats(queued + pending + completed, queued, pending, completed,
                users.countByRole(Role.AGENT), departments.count());
    }

    private static Map<Long, Counts> group(List<StatusCount> rows) {
        Map<Long, Counts> map = new HashMap<>();
        for (StatusCount row : rows) {
            Counts c = map.computeIfAbsent(row.getGroupId(), k -> new Counts());
            if (row.getStatus() == TicketStatus.OPEN) c.queued += row.getTotal();
            else if (row.getStatus().isPending()) c.pending += row.getTotal();
            else c.completed += row.getTotal();
        }
        return map;
    }

    private static final class Counts {
        static final Counts EMPTY = new Counts();
        long queued;
        long pending;
        long completed;
    }
}
