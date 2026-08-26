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

@Controller @RequestMapping("/agent")
public class AgentController {
    private final UserService users; private final TicketService tickets;
    public AgentController(UserService users, TicketService tickets) { this.users = users; this.tickets = tickets; }
    @GetMapping("/dashboard") public String dashboard(Model model, Principal principal) { User agent = users.findByUsername(principal.getName()); model.addAttribute("agent", agent); model.addAttribute("tickets", tickets.agentTickets(agent)); return "agent-dashboard"; }
    @PostMapping("/tickets/{id}/complete") public String complete(@PathVariable Long id, Principal principal, RedirectAttributes flash) { try { tickets.complete(id, users.findByUsername(principal.getName())); flash.addFlashAttribute("success", "Ticket marked complete; customer can now provide a rating."); } catch (RuntimeException ex) { flash.addFlashAttribute("error", ex.getMessage()); } return "redirect:/agent/dashboard"; }
    @GetMapping("/tickets/{id}/chat") public String chat(@PathVariable Long id, Model model, Principal principal) { User agent = users.findByUsername(principal.getName()); Ticket ticket = tickets.get(id); if (ticket.getAgent() == null || !ticket.getAgent().getId().equals(agent.getId())) return "error/403"; model.addAttribute("ticket", ticket); model.addAttribute("messages", tickets.messages(id, agent)); return "ticket-chat"; }
    @PostMapping("/tickets/{id}/messages") public String message(@PathVariable Long id, @RequestParam String body, Principal principal, RedirectAttributes flash) { try { tickets.sendMessage(id, users.findByUsername(principal.getName()), body); } catch (RuntimeException ex) { flash.addFlashAttribute("error", ex.getMessage()); } return "redirect:/agent/tickets/" + id + "/chat"; }
}
