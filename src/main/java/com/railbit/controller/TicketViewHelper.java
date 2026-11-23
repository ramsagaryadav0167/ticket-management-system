package com.railbit.controller;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import com.railbit.domain.Ticket;
import com.railbit.security.AppUserPrincipal;
import com.railbit.service.DepartmentService;
import com.railbit.service.PerformanceService;
import com.railbit.service.TicketService;

/** Builds the shared ticket detail page; each role sees the actions it is allowed to take. */
@Component
public class TicketViewHelper {

    private final TicketService tickets;
    private final DepartmentService departments;
    private final PerformanceService performance;

    public TicketViewHelper(TicketService tickets, DepartmentService departments, PerformanceService performance) {
        this.tickets = tickets;
        this.departments = departments;
        this.performance = performance;
    }

    /**
     * Data for the department-wise assign form: active departments, and the active agents who can
     * take tickets with their current pending load (the page filters agents by chosen department).
     */
    public void addAssignmentOptions(Model model) {
        model.addAttribute("departments", departments.findActive());
        model.addAttribute("assignableAgents", performance.allAgents().stream()
                .filter(a -> a.enabled() && a.departmentId() != null)
                .toList());
    }

    public String render(Long id, AppUserPrincipal user, String basePath, Model model) {
        Ticket ticket = tickets.getForView(id, user);
        model.addAttribute("ticket", ticket);
        model.addAttribute("basePath", basePath);
        model.addAttribute("messages", tickets.messages(id));
        model.addAttribute("activities", tickets.activities(id));
        model.addAttribute("rating", tickets.rating(id).orElse(null));
        if (user.isAdmin()) {
            addAssignmentOptions(model);
        }
        return "ticket/detail";
    }
}
