package com.railbit.controller;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.railbit.domain.Ticket;
import com.railbit.domain.TicketStatus;
import com.railbit.security.AppUserPrincipal;
import com.railbit.service.AgentService;
import com.railbit.service.DepartmentService;
import com.railbit.service.PerformanceService;
import com.railbit.service.TicketService;

/** Admin home, the ticket queue, ticket search and department-wise assignment. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final int PAGE_SIZE = 20;

    private final TicketService tickets;
    private final DepartmentService departments;
    private final AgentService agents;
    private final PerformanceService performance;
    private final TicketViewHelper view;

    public AdminController(TicketService tickets, DepartmentService departments, AgentService agents,
                           PerformanceService performance, TicketViewHelper view) {
        this.tickets = tickets;
        this.departments = departments;
        this.agents = agents;
        this.performance = performance;
        this.view = view;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", performance.dashboard());
        model.addAttribute("queue", tickets.queue());
        model.addAttribute("departmentSummaries", performance.departmentSummaries());
        model.addAttribute("agentPerformance", performance.allAgents());
        view.addAssignmentOptions(model);
        return "admin/dashboard";
    }

    @GetMapping("/tickets")
    public String tickets(@RequestParam(required = false) TicketStatus status,
                          @RequestParam(required = false) Long departmentId,
                          @RequestParam(required = false) Long agentId,
                          @RequestParam(required = false) String q,
                          @RequestParam(defaultValue = "0") int page,
                          Model model) {
        var pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("page", tickets.search(status, departmentId, agentId, q, pageable));
        model.addAttribute("statuses", TicketStatus.values());
        model.addAttribute("departments", departments.findAll());
        model.addAttribute("agents", agents.findAll());
        model.addAttribute("status", status);
        model.addAttribute("departmentId", departmentId);
        model.addAttribute("agentId", agentId);
        model.addAttribute("q", q);
        model.addAttribute("filterUrl", UriComponentsBuilder.fromPath("/admin/tickets")
                .queryParamIfPresent("status", Optional.ofNullable(status))
                .queryParamIfPresent("departmentId", Optional.ofNullable(departmentId))
                .queryParamIfPresent("agentId", Optional.ofNullable(agentId))
                .queryParamIfPresent("q", Optional.ofNullable(q).filter(s -> !s.isBlank()))
                .build().encode().toUriString());
        return "admin/tickets";
    }

    @GetMapping("/tickets/{id}")
    public String show(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal user, Model model) {
        return view.render(id, user, "/admin", model);
    }

    /** Assign to a chosen agent of a department, or (agentId empty) to its least-loaded agent. */
    @PostMapping("/tickets/{id}/assign")
    public String assign(@PathVariable Long id,
                         @RequestParam(required = false) Long departmentId,
                         @RequestParam(required = false) Long agentId,
                         @RequestParam(defaultValue = "/admin/dashboard") String returnTo,
                         @AuthenticationPrincipal AppUserPrincipal user, RedirectAttributes flash) {
        Ticket ticket = tickets.assign(id, departmentId, agentId, user);
        flash.addFlashAttribute("success", "Ticket #" + id + " assigned to " + ticket.getAgent().getFullName()
                + " (" + ticket.getDepartment().getName() + ").");
        return "redirect:" + safeLocalPath(returnTo);
    }

    @PostMapping("/tickets/{id}/close")
    public String close(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal user, RedirectAttributes flash) {
        tickets.close(id, user);
        flash.addFlashAttribute("success", "Ticket #" + id + " closed.");
        return "redirect:/admin/tickets/" + id;
    }

    @PostMapping("/tickets/{id}/messages")
    public String message(@PathVariable Long id, @RequestParam String body, @AuthenticationPrincipal AppUserPrincipal user) {
        tickets.postMessage(id, user, body);
        return "redirect:/admin/tickets/" + id + "#conversation";
    }

    /** Only allow redirects inside the admin area. */
    static String safeLocalPath(String path) {
        return path.startsWith("/admin/") && !path.startsWith("//") ? path : "/admin/dashboard";
    }
}
