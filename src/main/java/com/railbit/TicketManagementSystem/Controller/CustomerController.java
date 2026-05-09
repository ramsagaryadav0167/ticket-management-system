package com.railbit.TicketManagementSystem.Controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.railbit.TicketManagementSystem.Entity.*;
import com.railbit.TicketManagementSystem.Service.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

/**
 * 🎫 Customer Controller
 * Handles ticket creation, viewing, profile management, help, and history
 */
@Controller
@RequestMapping("/customer")
public class CustomerController {

    // 🔧 Inject services
    @Autowired private UserService userService;
    @Autowired private TicketService ticketService;
    @Autowired private TechnicalSupportDepartmentService departmentService;
    @Autowired private StorageService storageService;
    @Autowired private com.railbit.TicketManagementSystem.Repository.TicketHistoryRepository ticketHistoryRepository;
    @Autowired private NotificationService notificationService;
    /**
     * ✅ Load Customer Dashboard
     */
    @GetMapping("/dashboard")
    public String customerDashboard(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        String username = principal.getName();
        User user = userService.findByUsername(username);

        if (user != null && "ROLE_CUSTOMER".equalsIgnoreCase(user.getRole())) {
            List<Ticket> tickets = ticketService.findTicketsByUser(user);

            long totalTickets = tickets.size();
            long openTickets = tickets.stream().filter(t -> "Open".equalsIgnoreCase(t.getStatus())).count();
            long closedTickets = tickets.stream().filter(t -> "Closed".equalsIgnoreCase(t.getStatus())).count();
            long inProgressTickets = tickets.stream().filter(t -> "In Progress".equalsIgnoreCase(t.getStatus())).count();

            long slaBreachedCount = tickets.stream()
                .filter(Ticket::isSlaBreached)
                .count();

            long slaMetCount = tickets.stream()
                .filter(t -> !t.isSlaBreached() && "Closed".equalsIgnoreCase(t.getStatus()))
                .count();

            // Generate last 7 days dates and count tickets created each day
            List<String> dates = new ArrayList<>();
            List<Long> ticketsCount = new ArrayList<>();
            LocalDate today = LocalDate.now();

            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                dates.add(date.toString());

                long count = tickets.stream()
                    .filter(t -> t.getCreatedAt() != null)
                    .filter(t -> t.getCreatedAt().toLocalDate().isEqual(date))
                    .count();

                ticketsCount.add(count);
            }

            model.addAttribute("user", user);
            model.addAttribute("totalTickets", totalTickets);
            model.addAttribute("openTickets", openTickets);
            model.addAttribute("closedTickets", closedTickets);
            model.addAttribute("inProgressTickets", inProgressTickets);
            model.addAttribute("slaBreachedCount", slaBreachedCount);
            model.addAttribute("slaMetCount", slaMetCount);
            model.addAttribute("recentTickets", tickets.stream().limit(5).toList());

            // Add dates and ticketsCount for chart
            model.addAttribute("dates", dates);
            model.addAttribute("ticketsCount", ticketsCount);

            return "customer/dashboard";
        }

        return "error/403";
    }




    /**
     * ✅ Show Create Ticket Form
     */
    @GetMapping("/create_ticket")
    public String showCreateTicketForm(Model model) {
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "customer/create_ticket";
    }

    /**
     * ✅ Handle Ticket Form Submission
     */
    @PostMapping("/create_ticket")
    public String submitTicket(@ModelAttribute("ticket") Ticket ticket,
                               @RequestParam("file") MultipartFile file,
                               @RequestParam("departmentId") Long departmentId,
                               Principal principal) {

        String username = principal.getName();
        User user = userService.findByUsername(username);

        if (user == null || !"ROLE_CUSTOMER".equalsIgnoreCase(user.getRole())) {
            return "redirect:/access-denied";
        }

        // ✅ Ensure mobile number starts with +91
        if (ticket.getMobileNo() != null) {
            String digits = ticket.getMobileNo().replaceAll("\\D", "");
            if (!digits.matches("^[6-9][0-9]{9}$")) {
                throw new IllegalArgumentException("Invalid mobile number format");
            }
            ticket.setMobileNo(digits);
        }


        // Set ticket properties
        ticket.setUser(user);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setStatus(ticket.getStatus() == null ? "Open" : ticket.getStatus());
        ticket.setSlaDeadline(ticketService.calculateSlaDeadline(ticket.getPriority()));
        ticket.setSlaBreached(false);

        // Set department
        TechnicalSupportDepartment department = departmentService.getDepartmentById(departmentId);
        ticket.setTechnicalSupportDepartment(department);

        // Handle file upload
        if (file != null && !file.isEmpty()) {
            String uploadedFileName = storageService.storeFile(file);
            ticket.setFileUpload(uploadedFileName);
        }

        // Save ticket
        ticketService.saveTicket(ticket);

        // Save ticket creation to history
        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setType(HistoryType.CREATED);
        history.setDescription("Ticket created by customer");
        history.setUpdatedBy(user);
        ticketHistoryRepository.save(history);
        
     // ✅ ✅ Send notification to admin.
        User admin = userService.findAdmin(); // You must implement this in your UserService
        if (admin != null) {
            String message = "🆕 New ticket created by " + user.getFullName() + ": #" + ticket.getId();
            notificationService.sendNotification(admin, message);
        }

        return "redirect:/customer/view_tickets";
    }

    /**
     * ✅ List all tickets created by this customer
     */
    @GetMapping("/view_tickets")
    public String viewMyTickets(
            Model model,
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String username = principal.getName();
        User user = userService.findByUsername(username);

        if (user != null && "ROLE_CUSTOMER".equalsIgnoreCase(user.getRole())) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Ticket> ticketsPage = ticketService.findTicketsByUser(user, pageable);

            model.addAttribute("ticketsPage", ticketsPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", ticketsPage.getTotalPages());
        } else {
            model.addAttribute("ticketsPage", Page.empty());
        }

        return "customer/view_tickets";
    }

    /**
     * ✅ View specific ticket details
     */
    @GetMapping("/ticket/{id}")
    public String viewTicketDetail(@PathVariable Long id, Model model, Principal principal) {
        Ticket ticket = ticketService.findById(id);
        String username = principal.getName();

        if (ticket != null && ticket.getUser() != null &&
            ticket.getUser().getUsername().equals(username)) {
            model.addAttribute("ticket", ticket);
            return "customer/ticket_detail";
        }

        return "error/403";
    }

    /**
     * ✅ Delete a ticket (only if created within 24 hours)
     */
    @PostMapping("/delete-ticket/{id}")
    public String deleteCustomerTicket(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Ticket ticket = ticketService.findById(id);

        if (ticket != null && ticket.getUser() != null &&
            ticket.getUser().getUsername().equals(username)) {

            if (ticket.getCreatedAt().plusHours(24).isAfter(LocalDateTime.now())) {
                ticketService.deleteTicketById(id);
                redirectAttributes.addFlashAttribute("success", "Ticket deleted successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "You can only delete tickets within 24 hours of creation.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to delete this ticket.");
        }

        return "redirect:/customer/view_tickets";
    }

    /**
     * ✅ Show Help Page
     */
    @GetMapping("/help")
    public String showHelpForm(Model model) {
        model.addAttribute("helpRequest", new HelpRequest());
        return "customer/help";
    }

    /**
     * ✅ Submit Help Form
     */
    @PostMapping("/help")
    public String submitHelpForm(@ModelAttribute("helpRequest") HelpRequest helpRequest) {
        return "redirect:/help?success";
    }

    /**
     * ✅ View Profile
     */
    @GetMapping("/profile")
    public String customerProfile(Model model, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);

        if (user == null || !"ROLE_CUSTOMER".equalsIgnoreCase(user.getRole())) {
            return "redirect:/login?unauthorized";
        }

        model.addAttribute("user", user);
        return "customer/profile";
    }

    /**
     * ✅ Show Edit Profile Form
     */
    @GetMapping("/profile/edit")
    public String showEditProfileForm(Model model, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);

        if (user == null || !"ROLE_CUSTOMER".equalsIgnoreCase(user.getRole())) {
            return "redirect:/login?unauthorized";
        }

        model.addAttribute("user", user);
        return "customer/edit_profile";
    }

    /**
     * ✅ Handle Profile Update
     */
    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("user") User userForm,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        User existingUser = userService.findByUsername(principal.getName());

        if (existingUser == null || !"ROLE_CUSTOMER".equalsIgnoreCase(existingUser.getRole())) {
            return "redirect:/login?unauthorized";
        }

        existingUser.setFullName(userForm.getFullName());
        existingUser.setGender(userForm.getGender());
        existingUser.setAddress(userForm.getAddress());
        existingUser.setPhone(userForm.getPhone());

        userService.save(existingUser);

        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/customer/profile";
    }

    /**
     * ✅ Support Chat Page
     */
    @GetMapping("/support-chat")
    public String supportChat() {
        return "customer/support_chat";
    }

    /**
     * ✅ Services Static Page
     */
    @GetMapping("/services")
    public String customerServices() {
        return "customer/services";
    }

    /**
     * ✅ Ticket History View (Visible to Ticket Owner)
     */
    @GetMapping("/tickets/{ticketId}/history")
    public String viewTicketHistory(@PathVariable Long ticketId, Model model, Principal principal) {
        User customer = userService.findByUsername(principal.getName());
        Ticket ticket = ticketService.findById(ticketId);

        // 🔒 Security Check: Only allow customer who owns this ticket
        if (ticket == null || ticket.getUser() == null || ticket.getUser().getId() != customer.getId()) {
            return "error/403";
        }

        // Fetch history entries by ticket
        List<TicketHistory> history = ticketHistoryRepository.findByTicketIdOrderByTimestampDesc(ticketId);

        model.addAttribute("ticket", ticket);
        model.addAttribute("history", history);
        return "customer/ticket-history";
    }

}
