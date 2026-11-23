package com.railbit.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.railbit.domain.User;
import com.railbit.dto.AgentPerformance;
import com.railbit.dto.form.AgentForm;
import com.railbit.security.AppUserPrincipal;
import com.railbit.service.AgentService;
import com.railbit.service.DepartmentService;
import com.railbit.service.PerformanceService;
import com.railbit.service.TicketService;

import jakarta.validation.Valid;

/** All agents with their performance; create, move between departments, activate/deactivate. */
@Controller
@RequestMapping("/admin/agents")
public class AdminAgentController {

    private final AgentService agents;
    private final DepartmentService departments;
    private final PerformanceService performance;
    private final TicketService tickets;

    public AdminAgentController(AgentService agents, DepartmentService departments,
                                PerformanceService performance, TicketService tickets) {
        this.agents = agents;
        this.departments = departments;
        this.performance = performance;
        this.tickets = tickets;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long departmentId, Model model) {
        List<AgentPerformance> rows = departmentId == null
                ? performance.allAgents()
                : performance.forDepartment(departmentId);
        model.addAttribute("rows", rows);
        model.addAttribute("departmentId", departmentId);
        model.addAttribute("departments", departments.findAll());
        model.addAttribute("totals", new long[] {
                rows.stream().mapToLong(AgentPerformance::assigned).sum(),
                rows.stream().mapToLong(AgentPerformance::pending).sum(),
                rows.stream().mapToLong(AgentPerformance::completed).sum()});
        if (!model.containsAttribute("agentForm")) {
            model.addAttribute("agentForm", new AgentForm());
        }
        return "admin/agents";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("agentForm") AgentForm agentForm, BindingResult result,
                         Model model, RedirectAttributes flash) {
        AgentFormValidation.checkUnique(agentForm, result, agents);
        if (result.hasErrors()) {
            model.addAttribute("openAgentForm", true);
            return list(null, model);
        }
        User agent = agents.create(agentForm);
        flash.addFlashAttribute("success", "Agent " + agent.getFullName() + " created.");
        return "redirect:/admin/agents";
    }

    /** Agent profile: performance figures and current workload. */
    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model) {
        User agent = agents.get(id);
        model.addAttribute("agent", agent);
        model.addAttribute("stats", performance.forAgent(agent));
        model.addAttribute("pendingTickets", tickets.agentPendingTickets(id));
        model.addAttribute("completedTickets", tickets.agentCompletedTickets(id));
        model.addAttribute("departments", departments.findAll());
        return "admin/agent";
    }

    @PostMapping("/{id}/department")
    public String changeDepartment(@PathVariable Long id, @RequestParam(required = false) Long departmentId,
                                   @RequestParam(defaultValue = "/admin/agents") String returnTo,
                                   @AuthenticationPrincipal AppUserPrincipal admin, RedirectAttributes flash) {
        User agent = agents.get(id);
        int requeued = agents.changeDepartment(id, departmentId, admin);
        String where = departmentId == null ? "no department" : departments.get(departmentId).getName();
        flash.addFlashAttribute("success", agent.getFullName() + " now belongs to " + where + "."
                + AdminDepartmentController.requeuedNote(requeued));
        return "redirect:" + AdminController.safeLocalPath(returnTo);
    }

    @PostMapping("/{id}/enabled")
    public String setEnabled(@PathVariable Long id, @RequestParam boolean enabled,
                             @AuthenticationPrincipal AppUserPrincipal admin, RedirectAttributes flash) {
        User agent = agents.get(id);
        int requeued = agents.setEnabled(id, enabled, admin);
        flash.addFlashAttribute("success", agent.getFullName() + (enabled ? " activated." : " deactivated.")
                + AdminDepartmentController.requeuedNote(requeued));
        return "redirect:/admin/agents/" + id;
    }
}
