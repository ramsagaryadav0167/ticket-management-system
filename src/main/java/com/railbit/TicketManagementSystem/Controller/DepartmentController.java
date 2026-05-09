// Declares the package for organization
package com.railbit.TicketManagementSystem.Controller;

// Import required classes from your application
import com.railbit.TicketManagementSystem.Entity.TechnicalSupportDepartment;
import com.railbit.TicketManagementSystem.Entity.Ticket;
import com.railbit.TicketManagementSystem.Entity.User;
import com.railbit.TicketManagementSystem.Service.TicketService;
import com.railbit.TicketManagementSystem.Service.UserService;

// Spring Framework imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

// Marking this class as a Spring MVC Controller
@Controller
// Base path prefix for all endpoints in this controller (e.g., /department/tickets)
@RequestMapping("/department")
public class DepartmentController {

    // Inject the TicketService to fetch ticket data
    @Autowired
    private TicketService ticketService;

    // Inject the UserService to fetch user and their department
    @Autowired
    private UserService userService;

    // ✅ View tickets for the currently logged-in user's department
    @GetMapping("/tickets") // Handles GET requests to /department/tickets
    public String viewDepartmentTickets(Model model, Principal principal) {
        // Get the username of the currently logged-in user
        String username = principal.getName();

        // Fetch user entity from the database by username
        User user = userService.findByUsername(username);

        // Get the department associated with the logged-in user
        TechnicalSupportDepartment department = user.getTechnicalSupportDepartment();

        // Fetch all tickets assigned to that department
        List<Ticket> departmentTickets = ticketService.getTicketsByDepartmentId(department.getId());

        // Add ticket list to model to display in Thymeleaf page
        model.addAttribute("tickets", departmentTickets);

        // Return the view name (Thymeleaf template) to be rendered
        return "department_tickets"; // Ensure department_tickets.html exists
        
        
        
        
        //   i add   Department-wise feature
        
     
    	/*
    	 * @PostMapping("/manage-departments/{deptId}/tickets/{ticketId}/update-status")
    	 * public String updateStatusByDepartment(@PathVariable Long deptId,
    	 * 
    	 * @PathVariable Long ticketId,
    	 * 
    	 * @RequestParam("status") String newStatus,
    	 * 
    	 * @RequestParam(value = "assignedToId", required = false) Long assignedToId,
    	 * Principal principal, RedirectAttributes redirectAttributes) { return
    	 * updateTicketStatus(ticketId, newStatus, assignedToId, principal,
    	 * redirectAttributes); }
    	 * 
    	 * @GetMapping("/manage-departments/{deptId}/tickets/{ticketId}/details") public
    	 * String viewTicketHistoryByDepartment(@PathVariable Long deptId,
    	 * 
    	 * @PathVariable Long ticketId, Model model) { Ticket ticket =
    	 * ticketService.getTicketById(ticketId); model.addAttribute("ticket", ticket);
    	 * model.addAttribute("historyList", ticket.getHistoryList()); return
    	 * "ticket-details :: historyContent"; // Reuse existing Thymeleaf fragment }
    	 * 
    	 * @GetMapping("/manage-departments/{deptId}/tickets/{ticketId}/delete") public
    	 * String deleteTicketByDepartment(@PathVariable Long deptId,
    	 * 
    	 * @PathVariable Long ticketId, RedirectAttributes redirectAttributes) { try {
    	 * ticketService.deleteTicketById(ticketId);
    	 * redirectAttributes.addFlashAttribute("successMessage",
    	 * "Ticket deleted successfully."); } catch (Exception e) {
    	 * redirectAttributes.addFlashAttribute("errorMessage",
    	 * "Failed to delete ticket."); } return "redirect:/admin/manage-departments/" +
    	 * deptId; }
    	 * 
    	 * @GetMapping("/notifications/mark-as-read/{id}") public String
    	 * markNotificationAsRead(@PathVariable Long id, RedirectAttributes
    	 * redirectAttributes) { notificationService.markAsRead(id); return
    	 * "redirect:/admin/dashboard"; // Or wherever user was }
    	 */

    }
}
