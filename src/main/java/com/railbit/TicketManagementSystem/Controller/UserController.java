package com.railbit.TicketManagementSystem.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.railbit.TicketManagementSystem.DTO.PasswordChangeDto;
import com.railbit.TicketManagementSystem.Entity.TechnicalSupportDepartment;
import com.railbit.TicketManagementSystem.Entity.Ticket;
import com.railbit.TicketManagementSystem.Entity.TicketHistory;
import com.railbit.TicketManagementSystem.Entity.User;
import com.railbit.TicketManagementSystem.Repository.CreateTicketRepository;
import com.railbit.TicketManagementSystem.Repository.UserRepository;
import com.railbit.TicketManagementSystem.Service.NotificationService;
import com.railbit.TicketManagementSystem.Service.TechnicalSupportDepartmentService;
import com.railbit.TicketManagementSystem.Service.UserService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private com.railbit.TicketManagementSystem.Service.TicketService ticketService;

    @Autowired
    private com.railbit.TicketManagementSystem.Service.TicketHistoryService ticketHistoryService;
    
    @Autowired
    private CreateTicketRepository ticketRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    // Injects department service to fetch departments
    @Autowired
    private TechnicalSupportDepartmentService technicalSupportDepartmentService;

    /**
     * Show the form for creating a new ticket
     */
    @GetMapping("/user/create_ticket") // Maps GET requests to /create_ticket
    public String showCreateTicketForm(Model model) {
        Ticket ticket = new Ticket(); // Create new ticket instance
        ticket.setTechnicalSupportDepartment(new TechnicalSupportDepartment()); // 🟢 Initialize department to avoid null errors in form
        model.addAttribute("ticket", ticket); // Add empty ticket to model for form binding
        model.addAttribute("departments", technicalSupportDepartmentService.getAllDepartments()); // Add list of departments for dropdown
        return "create_ticket"; // Return Thymeleaf view
    }

    /**
     * Handle ticket creation form submission
     */
    @PostMapping("/user/create_ticket")
    public String createTicket(@ModelAttribute Ticket ticket,
                               @RequestParam("departmentId") Long departmentId,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(principal.getName());

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/user/create_ticket";
        }

        ticket.setUser(user);

        if (departmentId != null) {
            TechnicalSupportDepartment dept = technicalSupportDepartmentService.getDepartmentById(departmentId);
            if (dept != null) {
                ticket.setTechnicalSupportDepartment(dept);
            } else {
                redirectAttributes.addFlashAttribute("error", "Selected department does not exist.");
                return "redirect:/user/create_ticket";
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Please select a department.");
            return "redirect:/user/create_ticket";
        }

        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setSlaDeadline(LocalDateTime.now().plusHours(4));
        ticketService.saveTicket(ticket);

        // ✅ Notify admin
        User admin = userService.findAdmin();
        if (admin != null) {
            String message = "📝 New ticket created by " + user.getFullName() + ": #" + ticket.getId();
            notificationService.sendNotification(admin, message);
        }

        redirectAttributes.addFlashAttribute("success", "Ticket created successfully.");
        return "redirect:/user/view_tickets";
    }

   

    // ---------------------------------------------------------
    // ✅ Role-based Redirection: Show User Dashboard
    // ---------------------------------------------------------
    @GetMapping("/user/dashboard")
    public String dashboard(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            User user = userService.findByUsername(username);

            // ✅ Total Assigned Tickets
            int assignedTickets = ticketService.countByAssignedTo(user);

            // ✅ Open Tickets
            int openTickets = ticketService.countByAssignedToAndStatus(user, "OPEN");

            // ✅ Closed Tickets
            int closedTickets = ticketService.countByAssignedToAndStatus(user, "CLOSED");
            
            // ✅ In Progress Tickets
            int inProgressTickets = ticketService.countByAssignedToAndStatus(user, "In Progress");
            
         // ✅ Priority-wise ticket counts
            int lowPriority = ticketService.countByAssignedToAndPriority(user, "LOW");
            int mediumPriority = ticketService.countByAssignedToAndPriority(user, "MEDIUM");
            int highPriority = ticketService.countByAssignedToAndPriority(user, "HIGH");


            model.addAttribute("username", user.getUsername());
            model.addAttribute("assignedTickets", assignedTickets);
            model.addAttribute("openTickets", openTickets);
            model.addAttribute("closedTickets", closedTickets);
            model.addAttribute("inProgressTickets", inProgressTickets);
            
			
			model.addAttribute("lowPriority", lowPriority);
			model.addAttribute("mediumPriority", mediumPriority);
			model.addAttribute("highPriority", highPriority);


        }

        return "dashboard";
    }

    // ---------------------------------------------------------
    // ✅ Show Change Password Form
    // ---------------------------------------------------------
    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("passwordChangeDto", new PasswordChangeDto()); // Bind form DTO
        return "change-password";
    }

    // ---------------------------------------------------------
    // ✅ Handle Password Change Submission
    // ---------------------------------------------------------
    @PostMapping("/change-password")
    public String processPasswordChange(@ModelAttribute("passwordChangeDto") @Valid PasswordChangeDto dto,
                                        BindingResult result,
                                        Principal principal,
                                        Model model) {

        // Return to form if validation fails
        if (result.hasErrors()) {
            return "change-password";
        }

        // Get currently authenticated username
        String username = principal.getName();

        // Fetch user from DB
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Check if old password matches the current one
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            model.addAttribute("error", "Old password is incorrect.");
            return "change-password";
        }

        // Check if new password and confirm password match
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            model.addAttribute("error", "New passwords do not match.");
            return "change-password";
        }

        // Save new encoded password
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        model.addAttribute("success", "Password changed successfully.");
        return "change-password";
    }
    @PostMapping("/user/tickets/{id}/update-status")
    public String updateAssignedTicketStatus(@PathVariable Long id,
                                             @RequestParam("status") String newStatus,
                                             Principal principal,
                                             RedirectAttributes redirectAttributes) {
        Ticket ticket = ticketService.getTicketById(id);
        User currentUser = userService.getUserByUsername(principal.getName());

        if (ticket.getAssignedTo() == null || !Objects.equals(ticket.getAssignedTo().getId(), currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to update this ticket.");
            return "redirect:/user/tickets";
        }

        String oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus);
        ticketService.saveTicket(ticket);

        ticketHistoryService.log(
            com.railbit.TicketManagementSystem.Entity.HistoryType.STATUS_CHANGED,
            "Status changed from " + oldStatus + " to " + newStatus,
            ticket,
            currentUser
        );

        // ✅ Notify admin
        User admin = userService.findAdmin();
        if (admin != null) {
            String message = "🔄 Ticket #" + ticket.getId() + " status updated from " + oldStatus + " to " + newStatus + " by " + currentUser.getFullName();
            notificationService.sendNotification(admin, message);
        }

        redirectAttributes.addFlashAttribute("success", "Ticket status updated successfully.");
        return "redirect:/user/view_tickets";
    }

    
    
    @GetMapping("/user/ticket-history/{ticketId}")
    public String viewTicketHistory(@PathVariable Long ticketId, Model model) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));

        List<TicketHistory> historyList = ticketHistoryService.getHistoryByTicketId(ticketId);

        model.addAttribute("ticket", ticket);
        model.addAttribute("historyList", historyList);

        return "ticket-history";
    }

    /**
     * Show all tickets
     */
    @GetMapping("/user/view_tickets")
    public String viewAssignedTickets(
            Model model,
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String username = principal.getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Ticket> assignedTickets = ticketService.getTicketsAssignedTo(user, pageable);

        model.addAttribute("ticketsPage", assignedTickets);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", assignedTickets.getTotalPages());
        model.addAttribute("username", username);

        return "view_tickets";
    }
    
    /**
     * Delete ticket by ID
     */
    @PostMapping("/user/delete-ticket/{id}")
    public String deleteTicket(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(principal.getName());
            Ticket ticket = ticketService.getTicketById(id);

            ticketService.deleteTicketById(id);

            // ✅ Notify admin
            User admin = userService.findAdmin();
            if (admin != null) {
                String message = "❌ Ticket #" + id + " was deleted by " + user.getFullName();
                notificationService.sendNotification(admin, message);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Ticket deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting ticket.");
        }

        return "redirect:/user/view_tickets";
    }

}
