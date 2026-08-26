package com.railbit.Controller;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.railbit.Entity.Ticket;
import com.railbit.Entity.User;
import com.railbit.Service.TicketService;
import com.railbit.Service.UserService;

@Controller @RequestMapping("/customer")
public class CustomerController {
    private final UserService users; private final TicketService tickets;
    public CustomerController(UserService users, TicketService tickets) { this.users = users; this.tickets = tickets; }
    @GetMapping("/dashboard") public String dashboard(Model model, Principal principal) { User customer = users.findByUsername(principal.getName()); model.addAttribute("customer", customer); model.addAttribute("tickets", tickets.customerTickets(customer)); return "customer-dashboard"; }
    @PostMapping("/tickets") public String create(@RequestParam String title, @RequestParam String description, Principal principal, RedirectAttributes flash) {
        if (title.isBlank() || description.isBlank()) { flash.addFlashAttribute("error", "Title and description are required."); }
        else { tickets.create(users.findByUsername(principal.getName()), title, description); flash.addFlashAttribute("success", "Ticket created and sent to the admin."); }
        return "redirect:/customer/dashboard";
    }
    @GetMapping("/tickets/{id}/chat") public String chat(@PathVariable Long id, Model model, Principal principal) {
        User customer = users.findByUsername(principal.getName()); Ticket ticket = tickets.get(id);
        if (!ticket.getCustomer().getId().equals(customer.getId())) return "error/403";
        model.addAttribute("ticket", ticket); model.addAttribute("messages", tickets.messages(id, customer)); return "ticket-chat";
    }
    @PostMapping("/tickets/{id}/messages") public String message(@PathVariable Long id, @RequestParam String body, Principal principal, RedirectAttributes flash) {
        try { tickets.sendMessage(id, users.findByUsername(principal.getName()), body); } catch (RuntimeException ex) { flash.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/customer/tickets/" + id + "/chat";
    }
    @PostMapping("/tickets/{id}/rating") public String rating(@PathVariable Long id, @RequestParam int score, @RequestParam(required = false) String feedback, Principal principal, RedirectAttributes flash) {
        try { tickets.rate(id, users.findByUsername(principal.getName()), score, feedback); flash.addFlashAttribute("success", "Thanks for rating your agent."); }
        catch (RuntimeException ex) { flash.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/customer/dashboard";
    }
}
