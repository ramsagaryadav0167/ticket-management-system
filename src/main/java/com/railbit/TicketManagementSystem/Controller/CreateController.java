// Package name where this controller class is located
package com.railbit.TicketManagementSystem.Controller;

// Java and Spring imports
import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.railbit.TicketManagementSystem.Entity.Ticket;
import com.railbit.TicketManagementSystem.Entity.User;
import com.railbit.TicketManagementSystem.Entity.TechnicalSupportDepartment;
import com.railbit.TicketManagementSystem.Service.TechnicalSupportDepartmentService;
import com.railbit.TicketManagementSystem.Service.TicketService;
import com.railbit.TicketManagementSystem.Service.UserService;

// Marks this class as a Spring MVC controller
@Controller
public class CreateController {

    // Injects the TicketService to interact with ticket logic
    @Autowired
    private TicketService ticketService;

    // Injects the UserService to fetch user data
    @Autowired
    private UserService userService;

    // Injects department service to fetch departments
    @Autowired
    private TechnicalSupportDepartmentService technicalSupportDepartmentService;

   

    /**
     * Show update form for a ticket
     */
    @GetMapping("/user/edit-ticket/{id}") // Maps GET request for editing a specific ticket
    public String showUpdateForm(@PathVariable Long id, Model model) {
        Ticket ticket = ticketService.getTicketById(id); // Get ticket by ID
        if (ticket == null) {
            throw new RuntimeException("Ticket not found"); // If ticket not found, throw error
        }

        model.addAttribute("ticket", ticket); // Pass ticket to form
        model.addAttribute("departments", technicalSupportDepartmentService.getAllDepartments()); // Department list
        return "update_ticket"; // Thymeleaf page for editing
    }

    /**
     * Handle ticket update form submission
     */
    @PostMapping("/user/update-ticket")
    public String updateTicket(@ModelAttribute Ticket formTicket, Principal principal) {
        String username = principal.getName();
        User currentUser = userService.findByUsername(username);

        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        Ticket existingTicket = ticketService.getTicketById(formTicket.getId());
        if (existingTicket == null) {
            throw new RuntimeException("Ticket not found");
        }

        boolean isCustomer = currentUser.getRole().equals("ROLE_CUSTOMER");
        boolean isAgent = currentUser.getRole().equals("ROLE_USER");

        if (isCustomer) {
            // ROLE_CUSTOMER can update everything
            existingTicket.setTitle(formTicket.getTitle());
            existingTicket.setDescription(formTicket.getDescription());
            existingTicket.setPriority(formTicket.getPriority());
            existingTicket.setStatus(formTicket.getStatus());
            existingTicket.setSlaDeadline(formTicket.getSlaDeadline());
            existingTicket.setSlaBreached(formTicket.isSlaBreached());

            // Update department if changed
            Long deptId = formTicket.getTechnicalSupportDepartment() != null ? formTicket.getTechnicalSupportDepartment().getId() : null;
            if (deptId != null) {
                TechnicalSupportDepartment dept = technicalSupportDepartmentService.getDepartmentById(deptId);
                if (dept != null) {
                    existingTicket.setTechnicalSupportDepartment(dept);
                }
            }

        } else if (isAgent) {
            // ROLE_USER can update only selected fields
            existingTicket.setStatus(formTicket.getStatus());
            existingTicket.setPriority(formTicket.getPriority());
            existingTicket.setSlaDeadline(formTicket.getSlaDeadline());

            // Department update allowed for ROLE_USER
            Long deptId = formTicket.getTechnicalSupportDepartment() != null ? formTicket.getTechnicalSupportDepartment().getId() : null;
            if (deptId != null) {
                TechnicalSupportDepartment dept = technicalSupportDepartmentService.getDepartmentById(deptId);
                if (dept != null) {
                    existingTicket.setTechnicalSupportDepartment(dept);
                }
            }

            // ❌ Don't allow title/description/createdAt/user update
        }

        // Save updated ticket
        ticketService.updateTicket(existingTicket);
        return "redirect:/user/view_tickets";
    }


    


}
