package com.railbit.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.railbit.domain.User;
import com.railbit.security.AppUserPrincipal;
import com.railbit.service.AgentService;
import com.railbit.service.PerformanceService;
import com.railbit.service.TicketService;

@Controller
@RequestMapping("/agent")
public class AgentController {

    private final TicketService tickets;
    private final AgentService agents;
    private final PerformanceService performance;
    private final TicketViewHelper view;

    public AgentController(TicketService tickets, AgentService agents, PerformanceService performance,
                           TicketViewHelper view) {
        this.tickets = tickets;
        this.agents = agents;
        this.performance = performance;
        this.view = view;
    }

    /** The agent's own workload: assigned / pending / completed counts, rating and ticket lists. */
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "pending") String view,
                            @AuthenticationPrincipal AppUserPrincipal user, Model model) {
        User agent = agents.get(user.getId());
        boolean completed = "completed".equals(view);
        model.addAttribute("agent", agent);
        model.addAttribute("stats", performance.forAgent(agent));
        model.addAttribute("view", completed ? "completed" : "pending");
        model.addAttribute("tickets", completed
                ? tickets.agentCompletedTickets(user.getId())
                : tickets.agentPendingTickets(user.getId()));
        return "agent/dashboard";
    }

    @GetMapping("/tickets/{id}")
    public String show(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal user, Model model) {
        return view.render(id, user, "/agent", model);
    }

    @PostMapping("/tickets/{id}/start")
    public String start(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal user, RedirectAttributes flash) {
        tickets.start(id, user);
        flash.addFlashAttribute("success", "Ticket #" + id + " is now in progress.");
        return "redirect:/agent/tickets/" + id;
    }

    @PostMapping("/tickets/{id}/resolve")
    public String resolve(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal user, RedirectAttributes flash) {
        tickets.resolve(id, user);
        flash.addFlashAttribute("success", "Ticket #" + id + " resolved. The customer can now rate it or reopen it.");
        return "redirect:/agent/tickets/" + id;
    }

    @PostMapping("/tickets/{id}/messages")
    public String message(@PathVariable Long id, @RequestParam String body, @AuthenticationPrincipal AppUserPrincipal user) {
        tickets.postMessage(id, user, body);
        return "redirect:/agent/tickets/" + id + "#conversation";
    }
}
