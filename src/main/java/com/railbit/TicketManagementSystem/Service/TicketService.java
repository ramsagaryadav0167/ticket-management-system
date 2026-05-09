package com.railbit.TicketManagementSystem.Service;


import com.railbit.TicketManagementSystem.Entity.Ticket;
import com.railbit.TicketManagementSystem.Entity.TicketHistory;
import com.railbit.TicketManagementSystem.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.railbit.TicketManagementSystem.DTO.TicketStatsDTO;
import java.util.List;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketService {

    // CRUD operations
    void createTicket(Ticket ticket);
    void updateTicket(Ticket ticket);
    void deleteTicketById(Long id);
    void saveTicket(Ticket ticket);

    // Find tickets
    Ticket getTicketById(Long id);
    Ticket findById(Long id);
    List<Ticket> getAllTickets();
    List<Ticket> getTicketsByDepartmentId(Long deptId);
    List<Ticket> getTicketsByUser(User user);
    List<Ticket> findTicketsByUser(User user);

    // Assigned/unassigned tickets
    List<Ticket> getAssignedTickets();
    List<Ticket> getUnassignedTickets();
    Page<Ticket> findByAssigned(Pageable pageable);
    Page<Ticket> findByUnassigned(Pageable pageable);
    Page<Ticket> findAll(Pageable pageable);

    // Ticket history
    List<TicketHistory> getTicketHistoryByTicketId(Long ticketId);

    // Analytics
    long countAllTickets();
    long countSlaBreachedTickets();
    long countTicketsByStatus(String status);
    long countAssignedTickets();
    long countUnassignedTickets();

    // SLA
    LocalDateTime calculateSlaDeadline(String priority);

    // Latest ticket
    Optional<Ticket> getLatestTicketByCustomer(User customer);


    List<User> getAllAssignedAgentsByCustomer(User customer);
    
    //user  count 
    int countByAssignedTo(User user);

    int countByAssignedToAndStatus(User user, String status);

    //filter ticket
    Page<Ticket> filterTickets(String id, String title, String description, String createdBy, String assignedTo, String status, Pageable pageable);
   //customer pagination
    Page<Ticket> findTicketsByUser(User user, Pageable pageable);

    Page<Ticket> getTicketsAssignedTo(User user, Pageable pageable);
   
    
    //
    Page<Ticket> getTicketsByUserOrAssignedTo(User user, Pageable pageable);
    
  //priority user ticket
    int countByAssignedToAndPriority(User user, String priority);

   
}
