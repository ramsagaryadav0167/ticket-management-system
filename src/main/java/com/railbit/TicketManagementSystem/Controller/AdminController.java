package com.railbit.TicketManagementSystem.Controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.railbit.TicketManagementSystem.Entity.Notification;
import com.railbit.TicketManagementSystem.Entity.TechnicalSupportDepartment;
import com.railbit.TicketManagementSystem.Entity.Ticket;
import com.railbit.TicketManagementSystem.Entity.TicketHistory;
import com.railbit.TicketManagementSystem.Entity.User;
import com.railbit.TicketManagementSystem.Repository.CreateTicketRepository;
import com.railbit.TicketManagementSystem.Repository.UserRepository;
import com.railbit.TicketManagementSystem.Service.NotificationService;
import com.railbit.TicketManagementSystem.Service.TechnicalSupportDepartmentService;
import com.railbit.TicketManagementSystem.Service.TicketExportService;
import com.railbit.TicketManagementSystem.Service.TicketService;
import com.railbit.TicketManagementSystem.Service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller // Indicates this class handles web requests
@RequestMapping("/admin") // Prefix for all endpoints in this controller
public class AdminController {

    // === Service and Repository Dependencies ===
    @Autowired private UserRepository userRepository; // Repository for User entity
    @Autowired private UserService userService; // Service layer for user operations
    @Autowired private CreateTicketRepository ticketRepository; // Repository for Ticket entity
    @Autowired private TicketService ticketService; // Service layer for ticket operations
    @Autowired private TicketExportService ticketExportService; // Service to export tickets to CSV/Excel
    @Autowired private TechnicalSupportDepartmentService technicalSupportDepartmentService; // Service for managing departments
    @Autowired private com.railbit.TicketManagementSystem.Service.TicketHistoryService ticketHistoryService; // Service to handle ticket history
    @Autowired
    private NotificationService notificationService;
    // === Admin Dashboard ===
    @GetMapping("/dashboard") // Handles GET request for admin dashboard
    public String adminDashboard(Model model, Principal principal) {
        // Get current admin username and User object
        String username = principal.getName();
        User admin = userService.findByUsername(username);

        // Add statistics to the model
        model.addAttribute("username", username);
        model.addAttribute("totalTickets", ticketRepository.count());
        model.addAttribute("openTickets", ticketRepository.countByStatus("OPEN"));
        model.addAttribute("inProgressTickets", ticketRepository.countByStatus("In Progress"));
        model.addAttribute("ResolvedTickets", ticketRepository.countByStatus("Resolved"));
        model.addAttribute("assignedTickets", ticketService.countAssignedTickets());
        model.addAttribute("unassignedTickets", ticketService.countUnassignedTickets());
        model.addAttribute("totalUsers", userRepository.countByRole("ROLE_USER"));
        model.addAttribute("totalCustomers", userRepository.countByRole("ROLE_CUSTOMER"));
        model.addAttribute("slaBreachedTickets", ticketRepository.countBySlaBreachedTrue());
        model.addAttribute("closedTickets", ticketRepository.countByStatus("Closed"));

        
        return "admin-dashboard"; // View to render
    }


    // === List All Users ===
    @GetMapping("/users") // Endpoint to view all users
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll()); // Add all users to the model
        return "admin-users"; // View to display users
    }

    // === Delete User by ID ===
    @GetMapping("/users/delete/{id}") // Handles user deletion
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id); // Delete user by ID
        return "redirect:/admin/users"; // Redirect to users list
    }

    // === List All Tickets with Filter Options ===
    @GetMapping("/tickets") // Handles ticket listing with optional filters
    public String viewTickets(
            @RequestParam(defaultValue = "0") int page, // Page number for pagination
            @RequestParam(required = false) String id, // Optional filter by ID
            @RequestParam(required = false) String title, // Optional filter by title
            @RequestParam(required = false) String description, // Optional filter by description
            @RequestParam(required = false) String createdBy, // Optional filter by creator
            @RequestParam(required = false) String assignedTo, // Optional filter by assignee
            @RequestParam(required = false) String status, // Optional filter by status
            Model model // Model to pass data to view
    ) {
        Pageable pageable = PageRequest.of(page, 10); // Set pagination to 10 items per page
        Page<Ticket> tickets = ticketService.filterTickets(id, title, description, createdBy, assignedTo, status, pageable); // Fetch filtered tickets

        // Add ticket data and filter values to the model
        model.addAttribute("tickets", tickets);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tickets.getTotalPages());
        model.addAttribute("filterId", id);
        model.addAttribute("filterTitle", title);
        model.addAttribute("filterDescription", description);
        model.addAttribute("filterCreatedBy", createdBy);
        model.addAttribute("filterAssignedTo", assignedTo);
        model.addAttribute("filterStatus", status);
        model.addAttribute("users", userService.getUsersByRole("ROLE_USER"));
        return "admin-tickets"; // View to render ticket list
    }

    // === Delete Ticket by ID ===
    @GetMapping("/tickets/delete/{id}") // Handle ticket deletion by ID
    public String deleteTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ticketService.deleteTicketById(id); // Delete ticket
            redirectAttributes.addFlashAttribute("successMessage", "Ticket deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete ticket.");
        }
        return "redirect:/admin/tickets"; // Redirect to ticket list
    }

    // === Export Tickets to CSV ===
    @GetMapping("/export/csv") // Endpoint to export tickets in CSV format
    public void exportTicketsToCSV(HttpServletResponse response) throws IOException {
        ticketExportService.exportToCSV(response, ticketService.getAllTickets()); // Export logic
    }

    // === Export Tickets to Excel ===
    @GetMapping("/export/excel") // Endpoint to export tickets in Excel format
    public void exportTicketsToExcel(HttpServletResponse response) throws IOException {
        ticketExportService.exportToExcel(response, ticketService.getAllTickets()); // Export logic
    }

    // === Manage Departments ===
    @GetMapping("/manage-departments") // Displays all departments
    public String showDepartments(Model model) {
        model.addAttribute("departments", technicalSupportDepartmentService.getAllDepartments()); // Add departments to model
        return "manage-departments"; // Return view
    }

    // === View Tickets by Department ===
    @GetMapping("/manage-departments/{id}") // View tickets under specific department
    public String viewDepartmentTickets(@PathVariable Long id, Model model) {
        model.addAttribute("tickets", ticketService.getTicketsByDepartmentId(id)); // Tickets by department
        model.addAttribute("users", userService.getUsersByRole("ROLE_USER"));
        TechnicalSupportDepartment department = technicalSupportDepartmentService.getDepartmentById(id);
        model.addAttribute("departmentName", department.getDepartmentName());

        return "department_tickets"; // View to render
    }

    // === Show Assign Ticket Page ===
    @GetMapping("/tickets/{id}/assign") // Show assignment form for a ticket
    public String showAssignTicketPage(@PathVariable Long id, Model model) {
        model.addAttribute("ticket", ticketService.getTicketById(id)); // Ticket to assign
        model.addAttribute("users", userService.getUsersByRole("ROLE_USER")); // Get all agents
        return "assign-ticket"; // Return view
    }

    // === Assign Ticket to Agent ===
    @PostMapping("/tickets/{id}/assign") // Handle ticket assignment to an agent
    public String assignTicket(@PathVariable Long id, @RequestParam Long userId,
                               Principal principal, RedirectAttributes redirectAttributes) {
        Ticket ticket = ticketService.getTicketById(id); // Ticket to assign
        User assignedTo = userService.getUserById(userId); // Agent to assign
        User assignedBy = userService.getUserByUsername(principal.getName()); // Current admin user

        ticket.setAssignedTo(assignedTo); // Set assigned agent
        ticket.setAssignedBy(assignedBy); // Set assigned by
        ticket.setAssignedDate(LocalDateTime.now()); // Set timestamp
        ticket.setStatus("Assigned"); // Update ticket status
        ticketService.saveTicket(ticket); // Save changes

        // Log assignment to history
        ticketHistoryService.log(
            com.railbit.TicketManagementSystem.Entity.HistoryType.ASSIGNED,
            "Ticket assigned to agent: " + assignedTo.getFullName(),
            ticket,
            assignedBy
        );

        // Show confirmation message
        redirectAttributes.addFlashAttribute("success",
                "Ticket ID " + id + " successfully assigned to " + assignedTo.getFullName());

        return "redirect:/admin/tickets"; // Redirect to ticket list
    }

    // === View Agent Profile ===
    @GetMapping("/agents/{id}") // View agent's profile
    public String viewAgentProfile(@PathVariable Long id, Model model) {
        model.addAttribute("agent", userService.getUserById(id)); // Add agent to model
        return "agent-profile"; // View agent details
    }

    // === View Ticket Detail with History ===
    @GetMapping("/tickets/{id}/details") // View full history of a ticket
    public String viewTicketHistory(@PathVariable Long id, Model model) {
        Ticket ticket = ticketService.getTicketById(id); // Fetch ticket
        model.addAttribute("ticket", ticket); // Add ticket
        model.addAttribute("historyList", ticket.getHistoryList()); // Add history
        return "ticket-details :: historyContent"; // Return Thymeleaf fragment
    }

    // === Update Ticket Status + Optional Agent Assignment ===
    @PostMapping("/tickets/{id}/update-status") // Update status and/or assignment
    public String updateTicketStatus(@PathVariable Long id,
                                     @RequestParam("status") String newStatus,
                                     @RequestParam(value = "assignedToId", required = false) Long assignedToId,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {

        Ticket ticket = ticketService.getTicketById(id); // Fetch ticket
        if (ticket == null) {
            redirectAttributes.addFlashAttribute("error", "Ticket not found.");
            return "redirect:/admin/tickets";
        }

        String oldStatus = ticket.getStatus(); // Previous status
        User oldAssignee = ticket.getAssignedTo(); // Previous assignee
        User currentUser = userService.getUserByUsername(principal.getName()); // Logged-in admin

        // ✅ Update ticket status if changed
        if (!Objects.equals(oldStatus, newStatus)) {
            ticket.setStatus(newStatus);
            ticketHistoryService.log(
                com.railbit.TicketManagementSystem.Entity.HistoryType.STATUS_CHANGED,
                "Status changed from " + oldStatus + " to " + newStatus,
                ticket,
                currentUser
            );
        }

        // ✅ Update assigned agent if provided
        if (assignedToId != null) {
            User newAssignee = userService.getUserById(assignedToId);
            if (newAssignee != null && !Objects.equals(oldAssignee, newAssignee)) {
                ticket.setAssignedTo(newAssignee);
                ticket.setAssignedBy(currentUser);
                ticket.setAssignedDate(LocalDateTime.now());

                ticketHistoryService.log(
                    com.railbit.TicketManagementSystem.Entity.HistoryType.ASSIGNED,
                    "Assigned to agent: " + newAssignee.getFullName(),
                    ticket,
                    currentUser
                );
            }
        }

        ticketService.saveTicket(ticket); // Save ticket
        redirectAttributes.addFlashAttribute("success", "Ticket ID " + id + " updated successfully.");
        return "redirect:/admin/tickets"; // Redirect to ticket list
    }

    // === Show Department Page with Form and List ===
    @GetMapping("/technical-support") // View for managing departments
    public String viewDepartmentPage(Model model) {
        List<TechnicalSupportDepartment> departments = technicalSupportDepartmentService.getAllDepartments(); // Fetch all departments
        model.addAttribute("departments", departments); // Add list to model
        model.addAttribute("department", new TechnicalSupportDepartment()); // Empty object for create form
        return "technical_support_department"; // View name
    }

    // === Add a New Department ===
    @PostMapping("/technical-support/add")
    public String addDepartment(@Valid @ModelAttribute("department") TechnicalSupportDepartment department,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("department", department);
            return "admin/technical-support/add-technical-department"; // Make sure this HTML file exists
        }

        technicalSupportDepartmentService.createDepartment(department);
        redirectAttributes.addFlashAttribute("successMessage", "Department added successfully.");
        return "redirect:/admin/technical-support";
    }


    // === Delete Department by ID ===
    @GetMapping("/technical-support/delete/{id}") // Delete department
    public String deleteDepartment(@PathVariable Long id) {
        technicalSupportDepartmentService.deleteDepartmentById(id); // Perform deletion
        return "redirect:/admin/technical-support"; // Redirect to list
    }

 // === ✅ Load Edit Form for Department ===
    @GetMapping("/technical-support/edit/{id}")
    public String editDepartmentForm(@PathVariable Long id, Model model) {
        // 🔍 Retrieve department by ID
        TechnicalSupportDepartment department = technicalSupportDepartmentService.getDepartmentById(id);

        // 🛠️ Debug log
        System.out.println("Editing department: ID = " + department.getId());

        // 💾 Add the department object to the model to populate the form
        model.addAttribute("department", department);

        // 🌐 Return the edit form view
        return "edit-technical-department";
    }

    // === ✅ Handle Department Update ===
    @PostMapping("/technical-support/update")
    public String updateDepartment(@ModelAttribute("department") TechnicalSupportDepartment department) {
        // 🛠️ Debug log
        System.out.println("Updating department with ID: " + department.getId());

        // 💾 Ensure we are updating, not creating a new record
        if (department.getId() != null) {
            technicalSupportDepartmentService.updateDepartment(department);
        } else {
            System.out.println("Warning: Department ID is null. Skipping update.");
        }

        // ✅ Redirect back to the department listing
        return "redirect:/admin/technical-support";
    }

    
    // ✅ Handles your current <a href="..."> (GET)
    @GetMapping("/notifications/mark-as-read/{id}")
    public String markAsReadGet(@PathVariable Long id,
                                @RequestHeader(value = "Referer", required = false) String referer) {
        notificationService.markAsRead(id);
        // Redirect back to the page that opened the modal (dashboard, tickets page, etc.)
        return "redirect:" + (referer != null ? referer : "/admin/dashboard");
    }

    // ✅ Optional: AJAX/POST endpoint if you switch to fetch()
    @PostMapping("notifications/mark-as-read/{id}")
    @ResponseBody
    public ResponseEntity<?> markAsReadPost(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
 
}
