package com.railbit.Controller;

import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.railbit.Entity.Ticket;
import com.railbit.Entity.User;
import com.railbit.Service.TicketService;
import com.railbit.Service.UserService;

@Controller @RequestMapping("/admin")
public class AdminController {
    private final UserService users; private final TicketService tickets;
    public AdminController(UserService users, TicketService tickets) { this.users = users; this.tickets = tickets; }
    @GetMapping("/dashboard") public String dashboard(Model model, Principal principal) {
        List<User> agents = users.agents();
        model.addAttribute("admin", users.findByUsername(principal.getName()));
        model.addAttribute("agents", agents); model.addAttribute("openTickets", tickets.unassignedTickets());
        model.addAttribute("agent", new User()); model.addAttribute("ticketService", tickets);
        return "admin-dashboard";
    }
    @PostMapping("/agents") public String createAgent(@ModelAttribute User agent, RedirectAttributes flash) {
        try { users.createAgent(agent); flash.addFlashAttribute("success", "Agent created successfully."); }
        catch (IllegalArgumentException ex) { flash.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/admin/dashboard";
    }
    @PostMapping("/tickets/{id}/assign") public String assign(@PathVariable Long id, RedirectAttributes flash) {
        try { Ticket ticket = tickets.assignLeastLoaded(id); flash.addFlashAttribute("success", "Ticket #" + ticket.getId() + " assigned to " + ticket.getAgent().getFullName() + "."); }
        catch (RuntimeException ex) { flash.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/admin/dashboard";
    }
}
