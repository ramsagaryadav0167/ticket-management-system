package com.railbit.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.railbit.domain.Department;
import com.railbit.domain.Role;
import com.railbit.domain.Ticket;
import com.railbit.domain.TicketActivity;
import com.railbit.domain.TicketMessage;
import com.railbit.domain.TicketRating;
import com.railbit.domain.TicketStatus;
import com.railbit.domain.User;
import com.railbit.dto.form.TicketForm;
import com.railbit.exception.BusinessException;
import com.railbit.exception.NotFoundException;
import com.railbit.repository.TicketActivityRepository;
import com.railbit.repository.TicketMessageRepository;
import com.railbit.repository.TicketRatingRepository;
import com.railbit.repository.TicketRepository;
import com.railbit.repository.UserRepository;
import com.railbit.security.AppUserPrincipal;

/**
 * Ticket workflow: customers raise tickets into a department queue, admins assign them to an agent
 * of that department (manually or to the least-loaded agent), agents work and resolve them, and
 * customers rate (close) or reopen them.
 */
@Service
@Transactional
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository tickets;
    private final TicketMessageRepository messages;
    private final TicketActivityRepository activities;
    private final TicketRatingRepository ratings;
    private final UserRepository users;
    private final DepartmentService departments;

    public TicketService(TicketRepository tickets, TicketMessageRepository messages,
                         TicketActivityRepository activities, TicketRatingRepository ratings,
                         UserRepository users, DepartmentService departments) {
        this.tickets = tickets;
        this.messages = messages;
        this.activities = activities;
        this.ratings = ratings;
        this.users = users;
        this.departments = departments;
    }

    // ------------------------------------------------------------------ queries

    @Transactional(readOnly = true)
    public List<Ticket> customerTickets(Long customerId) {
        return tickets.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Transactional(readOnly = true)
    public List<Ticket> agentPendingTickets(Long agentId) {
        return tickets.findByAgentIdAndStatusInOrderByUpdatedAtDesc(agentId, TicketStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Ticket> agentCompletedTickets(Long agentId) {
        return tickets.findByAgentIdAndStatusInOrderByUpdatedAtDesc(agentId, TicketStatus.COMPLETED);
    }

    /** Unassigned tickets, most urgent first, then oldest first. */
    @Transactional(readOnly = true)
    public List<Ticket> queue() {
        return tickets.findByStatusOrderByCreatedAtAsc(TicketStatus.OPEN).stream()
                .sorted(Comparator.comparing((Ticket t) -> t.getPriority().ordinal()).reversed()
                        .thenComparing(Ticket::getCreatedAt))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<Ticket> search(TicketStatus status, Long departmentId, Long agentId, String q, Pageable pageable) {
        String query = q == null || q.isBlank() ? null : q.trim();
        return tickets.search(status, departmentId, agentId, query, pageable);
    }

    /** Loads a ticket and checks the user may see it: admins all, agents their own, customers their own. */
    @Transactional(readOnly = true)
    public Ticket getForView(Long id, AppUserPrincipal user) {
        Ticket ticket = load(id);
        requireParticipant(ticket, user);
        return ticket;
    }

    @Transactional(readOnly = true)
    public List<TicketMessage> messages(Long ticketId) {
        return messages.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    @Transactional(readOnly = true)
    public List<TicketActivity> activities(Long ticketId) {
        return activities.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    @Transactional(readOnly = true)
    public Optional<TicketRating> rating(Long ticketId) {
        return ratings.findByTicketId(ticketId);
    }

    // ------------------------------------------------------------------ customer actions

    public Ticket create(TicketForm form, Long customerId) {
        Department department = departments.get(form.getDepartmentId());
        if (!department.isActive()) {
            throw new BusinessException("The " + department.getName() + " department is not accepting tickets.");
        }
        User customer = users.getReferenceById(customerId);
        Ticket ticket = new Ticket();
        ticket.setTitle(form.getTitle().trim());
        ticket.setDescription(form.getDescription().trim());
        ticket.setPriority(form.getPriority());
        ticket.setDepartment(department);
        ticket.setCustomer(customer);
        tickets.save(ticket);
        record(ticket, customer, "Ticket created in " + department.getName());
        return ticket;
    }

    public void rate(Long ticketId, AppUserPrincipal customer, int score, String feedback) {
        Ticket ticket = load(ticketId);
        requireOwner(ticket, customer);
        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new BusinessException("You can rate a ticket once the agent has resolved it.");
        }
        if (score < 1 || score > 5) {
            throw new BusinessException("Rating must be between 1 and 5.");
        }
        if (ratings.existsByTicketId(ticketId)) {
            throw new BusinessException("This ticket has already been rated.");
        }
        TicketRating rating = new TicketRating();
        rating.setTicket(ticket);
        rating.setAgent(ticket.getAgent());
        rating.setScore(score);
        rating.setFeedback(feedback == null || feedback.isBlank() ? null : feedback.trim());
        ratings.save(rating);

        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(LocalDateTime.now());
        record(ticket, users.getReferenceById(customer.getId()), "Rated " + score + "/5 and closed");
    }

    public void reopen(Long ticketId, AppUserPrincipal customer) {
        Ticket ticket = load(ticketId);
        requireOwner(ticket, customer);
        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new BusinessException("Only resolved tickets can be reopened.");
        }
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setResolvedAt(null);
        record(ticket, users.getReferenceById(customer.getId()), "Reopened by customer");
    }

    // ------------------------------------------------------------------ admin actions

    /**
     * Assigns (or reassigns) a ticket.
     *
     * @param departmentId department to route to; may differ from the ticket's current one
     * @param agentId      a specific agent of that department, or null to pick the least-loaded one
     */
    public Ticket assign(Long ticketId, Long departmentId, Long agentId, AppUserPrincipal admin) {
        Ticket ticket = load(ticketId);
        if (ticket.getStatus().isCompleted()) {
            throw new BusinessException("Ticket #" + ticketId + " is already " + ticket.getStatus().label().toLowerCase() + ".");
        }
        Department department = departments.get(departmentId != null ? departmentId : ticket.getDepartment().getId());
        if (!department.isActive()) {
            throw new BusinessException("The " + department.getName() + " department is inactive.");
        }

        User agent = agentId != null
                ? requireAssignableAgent(agentId, department)
                : leastLoadedAgent(department, ticket.getAgent() == null ? null : ticket.getAgent().getId());
        if (ticket.isAssignedTo(agent.getId())) {
            throw new BusinessException("Ticket #" + ticketId + " is already assigned to " + agent.getFullName() + ".");
        }

        User previous = ticket.getAgent();
        boolean departmentChanged = !department.getId().equals(ticket.getDepartment().getId());
        ticket.setDepartment(department);
        ticket.setAgent(agent);
        ticket.setStatus(TicketStatus.ASSIGNED);
        ticket.setAssignedAt(LocalDateTime.now());

        String action = previous == null ? "Assigned to " : "Reassigned from " + previous.getFullName() + " to ";
        record(ticket, users.getReferenceById(admin.getId()), action + agent.getFullName()
                + (departmentChanged ? " (moved to " + department.getName() + ")" : "")
                + (agentId == null ? " – auto, least loaded" : ""));
        log.info("Ticket {} assigned to agent {} by admin {}", ticketId, agent.getId(), admin.getId());
        return ticket;
    }

    public void close(Long ticketId, AppUserPrincipal admin) {
        Ticket ticket = load(ticketId);
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BusinessException("Ticket #" + ticketId + " is already closed.");
        }
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(LocalDateTime.now());
        record(ticket, users.getReferenceById(admin.getId()), "Closed by admin");
    }

    /**
     * Puts an agent's unfinished tickets back into their department queue. Called when an agent
     * leaves a department or is deactivated, so no customer is left waiting on someone who can't help.
     *
     * @return how many tickets were returned to the queue
     */
    public int requeueAgentTickets(Long agentId, AppUserPrincipal admin, String reason) {
        List<Ticket> open = tickets.findByAgentIdAndStatusIn(agentId, TicketStatus.PENDING);
        User actor = users.getReferenceById(admin.getId());
        for (Ticket ticket : open) {
            ticket.setAgent(null);
            ticket.setStatus(TicketStatus.OPEN);
            ticket.setAssignedAt(null);
            record(ticket, actor, "Returned to queue: " + reason);
        }
        return open.size();
    }

    // ------------------------------------------------------------------ agent actions

    public void start(Long ticketId, AppUserPrincipal agent) {
        Ticket ticket = load(ticketId);
        requireAssignee(ticket, agent);
        if (ticket.getStatus() != TicketStatus.ASSIGNED) {
            throw new BusinessException("Only newly assigned tickets can be started.");
        }
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        record(ticket, users.getReferenceById(agent.getId()), "Work started");
    }

    public void resolve(Long ticketId, AppUserPrincipal agent) {
        Ticket ticket = load(ticketId);
        requireAssignee(ticket, agent);
        if (!ticket.getStatus().isPending()) {
            throw new BusinessException("Only tickets you are working on can be resolved.");
        }
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolvedAt(LocalDateTime.now());
        record(ticket, users.getReferenceById(agent.getId()), "Marked as resolved");
    }

    // ------------------------------------------------------------------ conversation

    public void postMessage(Long ticketId, AppUserPrincipal sender, String body) {
        Ticket ticket = load(ticketId);
        requireParticipant(ticket, sender);
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BusinessException("This ticket is closed.");
        }
        if (body == null || body.isBlank()) {
            throw new BusinessException("Message cannot be empty.");
        }
        if (body.length() > 3000) {
            throw new BusinessException("Message is too long (3000 characters max).");
        }
        TicketMessage message = new TicketMessage();
        message.setTicket(ticket);
        message.setSender(users.getReferenceById(sender.getId()));
        message.setBody(body.trim());
        messages.save(message);
    }

    // ------------------------------------------------------------------ helpers

    private Ticket load(Long id) {
        return tickets.findDetailedById(id).orElseThrow(() -> new NotFoundException("Ticket #" + id + " not found"));
    }

    private User requireAssignableAgent(Long agentId, Department department) {
        User agent = users.findWithDepartmentById(agentId)
                .filter(User::isAgent)
                .orElseThrow(() -> new NotFoundException("Agent not found"));
        if (!agent.isEnabled()) {
            throw new BusinessException(agent.getFullName() + " is inactive and cannot take tickets.");
        }
        if (agent.getDepartment() == null || !agent.getDepartment().getId().equals(department.getId())) {
            throw new BusinessException(agent.getFullName() + " is not a member of " + department.getName() + ".");
        }
        return agent;
    }

    /**
     * Active agent of the department with the fewest pending tickets (ties: lowest id, i.e. longest serving).
     * When reassigning, the current agent is excluded so the ticket actually moves.
     */
    private User leastLoadedAgent(Department department, Long excludeAgentId) {
        return users.findByRoleAndDepartmentIdAndEnabledTrue(Role.AGENT, department.getId()).stream()
                .filter(a -> !a.getId().equals(excludeAgentId))
                .min(Comparator.comparingLong((User a) -> tickets.countByAgentIdAndStatusIn(a.getId(), TicketStatus.PENDING))
                        .thenComparing(User::getId))
                .orElseThrow(() -> new BusinessException(
                        "The " + department.getName() + " department has no available agent. Add or activate an agent in this department first."));
    }

    private void record(Ticket ticket, User actor, String description) {
        activities.save(new TicketActivity(ticket, actor, description));
    }

    private static void requireParticipant(Ticket ticket, AppUserPrincipal user) {
        boolean allowed = switch (user.getRole()) {
            case ADMIN -> true;
            case AGENT -> ticket.isAssignedTo(user.getId());
            case CUSTOMER -> ticket.isOwnedBy(user.getId());
        };
        if (!allowed) {
            throw new AccessDeniedException("You do not have access to ticket #" + ticket.getId());
        }
    }

    private static void requireOwner(Ticket ticket, AppUserPrincipal user) {
        if (!ticket.isOwnedBy(user.getId())) {
            throw new AccessDeniedException("Not your ticket");
        }
    }

    private static void requireAssignee(Ticket ticket, AppUserPrincipal user) {
        if (!ticket.isAssignedTo(user.getId())) {
            throw new AccessDeniedException("This ticket is not assigned to you");
        }
    }
}
