package com.railbit.TicketManagementSystem.Service;


import com.railbit.TicketManagementSystem.Entity.HistoryType;
import com.railbit.TicketManagementSystem.Entity.Ticket;
import com.railbit.TicketManagementSystem.Entity.TicketHistory;
import com.railbit.TicketManagementSystem.Entity.User;
import com.railbit.TicketManagementSystem.Repository.CreateTicketRepository;
import com.railbit.TicketManagementSystem.Repository.TicketHistoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;
import java.util.Optional;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private CreateTicketRepository ticketRepository;

    @Autowired
    private TicketHistoryRepository ticketHistoryRepository;
    
    @Autowired
    private TicketHistoryService ticketHistoryService; 

    // Create a new ticket with SLA
    @Override
    public void createTicket(Ticket ticket) {
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setSlaDeadline(calculateSlaDeadline(ticket.getPriority()));
        ticket.setSlaBreached(false);
        ticketRepository.save(ticket);
    }

    // Calculate SLA deadline based on priority
    @Override
    public LocalDateTime calculateSlaDeadline(String priority) {
        LocalDateTime now = LocalDateTime.now();
        switch (priority.toUpperCase()) {
            case "HIGH": return now.plusHours(4);
            case "MEDIUM": return now.plusHours(12);
            case "LOW":
            default: return now.plusHours(24);
        }
    }

    // Get all tickets and mark SLA breached if overdue
    @Override
    public List<Ticket> getAllTickets() {
        List<Ticket> tickets = ticketRepository.findAllByOrderByCreatedAtDesc();
        for (Ticket ticket : tickets) {
            LocalDateTime deadline = ticket.getSlaDeadline();
            if (deadline != null && !ticket.isSlaBreached() && LocalDateTime.now().isAfter(deadline)) {
                ticket.setSlaBreached(true);
                ticketRepository.save(ticket);
            }
        }
        return tickets;
    }

    // Get ticket by ID (exception if not found)
    @Override
    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with ID: " + id));
    }

    // Get ticket by ID (returns null if not found)
    @Override
    public Ticket findById(Long id) {
        return ticketRepository.findById(id).orElse(null);
    }

    // Update an existing ticket
    @Override
    public void updateTicket(Ticket ticket) {
        Ticket existingTicket = ticketRepository.findById(ticket.getId())
            .orElseThrow(() -> new RuntimeException("Ticket not found with ID: " + ticket.getId()));

        User agentUser = ticket.getAssignedTo(); // Agent who is updating

        if (!existingTicket.getStatus().equals(ticket.getStatus())) {
            HistoryType type = HistoryType.STATUS_CHANGED;
            String description = "Status changed from " + existingTicket.getStatus() +
                                 " to " + ticket.getStatus();

            existingTicket.setStatus(ticket.getStatus());

            // ✅ Log history using injected service
            ticketHistoryService.log(type, description, existingTicket, agentUser);

            // ✅ Save updated ticket
            ticketRepository.save(existingTicket);
        }
    }




    // Delete ticket by ID
    @Override
    public void deleteTicketById(Long id) {
        ticketRepository.deleteById(id);
    }

    // Save a ticket (alias method)
    @Override
    public void saveTicket(Ticket ticket) {
        ticketRepository.save(ticket);
    }

    // Get tickets by department ID
    @Override
    public List<Ticket> getTicketsByDepartmentId(Long deptId) {
        return ticketRepository.findByTechnicalSupportDepartmentId(deptId);
    }

    // Get tickets assigned to a specific agent
    @Override
    public List<Ticket> getTicketsByUser(User user) {
        return ticketRepository.findByAssignedTo(user);
    }

    // Get tickets created by a user (customer)
    @Override
    public List<Ticket> findTicketsByUser(User user) {
        return ticketRepository.findByUser(user);
    }

    // Get all assigned tickets
    @Override
    public List<Ticket> getAssignedTickets() {
        return ticketRepository.findByAssignedToIsNotNull();
    }

    // Get all unassigned tickets
    @Override
    public List<Ticket> getUnassignedTickets() {
        return ticketRepository.findByAssignedToIsNull();
    }

    // Count all tickets
    @Override
    public long countAllTickets() {
        return ticketRepository.count();
    }

    // Count tickets by status
    @Override
    public long countTicketsByStatus(String status) {
        return ticketRepository.countByStatus(status);
    }

    // Count SLA-breached tickets
    @Override
    public long countSlaBreachedTickets() {
        return ticketRepository.countBySlaBreachedTrue();
    }

    // Count assigned tickets
    @Override
    public long countAssignedTickets() {
        return ticketRepository.countByAssignedToIsNotNull();
    }

    // Count unassigned tickets
    @Override
    public long countUnassignedTickets() {
        return ticketRepository.countByAssignedToIsNull();
    }

    // Get ticket history by ticket ID
    @Override
    public List<TicketHistory> getTicketHistoryByTicketId(Long ticketId) {
        return ticketHistoryRepository.findByTicketIdOrderByTimestampDesc(ticketId);
    }
    
    // Customer i add
 

    @Override
    public Optional<Ticket> getLatestTicketByCustomer(User customer) {
        return ticketRepository.findTop1ByUserOrderByCreatedAtDesc(customer);
    }

    @Override
    public Page<Ticket> findByAssigned(Pageable pageable) {
        return ticketRepository.findByAssignedToIsNotNull(pageable);
    }

    @Override
    public Page<Ticket> findByUnassigned(Pageable pageable) {
        return ticketRepository.findByAssignedToIsNull(pageable);
    }

    @Override
    public Page<Ticket> findAll(Pageable pageable) {
        return ticketRepository.findAll(pageable);
    }

   //chat all assign agent
 // TicketServiceImpl.java

    @Override
    public List<User> getAllAssignedAgentsByCustomer(User customer) {
        return ticketRepository.findDistinctAssignedAgentsByUser(customer);
    }

    // user  count
    @Override
    public int countByAssignedTo(User user) {
        return ticketRepository.countByAssignedTo(user);
    }

    @Override
    public int countByAssignedToAndStatus(User user, String status) {
        return ticketRepository.countByAssignedToAndStatus(user, status);
    }
    
    //filter
    @Override
    public Page<Ticket> filterTickets(String id, String title, String description, String createdBy, String assignedTo, String status, Pageable pageable) {
        return ticketRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (id != null && !id.isEmpty()) {
                try {
                    predicates.add(cb.equal(root.get("id"), Long.parseLong(id)));
                } catch (NumberFormatException e) {
                    // Invalid ID input
                }
            }

            if (title != null && !title.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }

            if (description != null && !description.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%"));
            }

            if (createdBy != null && !createdBy.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("user").get("username")), "%" + createdBy.toLowerCase() + "%"));
            }

            if (assignedTo != null && !assignedTo.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("assignedTo").get("fullName")), "%" + assignedTo.toLowerCase() + "%"));
            }

            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }

    //customer page pagination
    
    @Override
    public Page<Ticket> findTicketsByUser(User user, Pageable pageable) {
        return ticketRepository.findByUser(user, pageable);
    }
   

    @Override
    public Page<Ticket> getTicketsAssignedTo(User user, Pageable pageable) {
        return ticketRepository.findByAssignedTo(user, pageable);
    }
  
    @Override
    public Page<Ticket> getTicketsByUserOrAssignedTo(User user, Pageable pageable) {
        return ticketRepository.findByUserOrAssignedTo(user, user, pageable);
    }
   //
    @Override
    public int countByAssignedToAndPriority(User user, String priority) {
        if (user == null || priority == null) {
            return 0;
        }
        return ticketRepository.countByAssignedToAndPriority(user, priority);
    }

}
