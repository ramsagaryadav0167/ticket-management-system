package com.railbit.controller;

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

import com.railbit.domain.Department;
import com.railbit.domain.User;
import com.railbit.dto.form.AgentForm;
import com.railbit.dto.form.DepartmentForm;
import com.railbit.security.AppUserPrincipal;
import com.railbit.service.AgentService;
import com.railbit.service.DepartmentService;
import com.railbit.service.PerformanceService;

import jakarta.validation.Valid;

/** Departments and their teams: create/edit departments, add or remove agents. */
@Controller
@RequestMapping("/admin/departments")
public class AdminDepartmentController {

    private final DepartmentService departments;
    private final AgentService agents;
    private final PerformanceService performance;

    public AdminDepartmentController(DepartmentService departments, AgentService agents, PerformanceService performance) {
        this.departments = departments;
        this.agents = agents;
        this.performance = performance;
    }

    @GetMapping
    public String list(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new DepartmentForm());
        }
        model.addAttribute("summaries", performance.departmentSummaries());
        return "admin/departments";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") DepartmentForm form, BindingResult result,
                         Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            return list(model);
        }
        Department department = departments.create(form);
        flash.addFlashAttribute("success", "Department '" + department.getName() + "' created. Now add agents to it.");
        return "redirect:/admin/departments/" + department.getId();
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model) {
        Department department = departments.get(id);
        model.addAttribute("department", department);
        model.addAttribute("summary", performance.departmentSummary(id));
        model.addAttribute("team", performance.forDepartment(id));
        model.addAttribute("candidates", agents.findNotInDepartment(id));
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", DepartmentForm.from(department));
        }
        if (!model.containsAttribute("agentForm")) {
            model.addAttribute("agentForm", new AgentForm());
        }
        return "admin/department";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") DepartmentForm form,
                         BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            return show(id, model);
        }
        departments.update(id, form);
        flash.addFlashAttribute("success", "Department details saved.");
        return "redirect:/admin/departments/" + id;
    }

    @PostMapping("/{id}/active")
    public String setActive(@PathVariable Long id, @RequestParam boolean active, RedirectAttributes flash) {
        Department department = departments.setActive(id, active);
        flash.addFlashAttribute("success", department.getName() + (active
                ? " is active and accepting tickets."
                : " is inactive. Customers can no longer raise tickets for it."));
        return "redirect:/admin/departments/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        String name = departments.get(id).getName();
        departments.delete(id);
        flash.addFlashAttribute("success", "Department '" + name + "' deleted.");
        return "redirect:/admin/departments";
    }

    /** Grow the team with an existing agent (moved from another department or unassigned). */
    @PostMapping("/{id}/agents")
    public String addExistingAgent(@PathVariable Long id, @RequestParam Long agentId,
                                   @AuthenticationPrincipal AppUserPrincipal admin, RedirectAttributes flash) {
        User agent = agents.get(agentId);
        int requeued = agents.changeDepartment(agentId, id, admin);
        flash.addFlashAttribute("success", agent.getFullName() + " added to " + departments.get(id).getName() + "."
                + requeuedNote(requeued));
        return "redirect:/admin/departments/" + id;
    }

    /** Grow the team with a brand-new agent account. */
    @PostMapping("/{id}/agents/new")
    public String createAgent(@PathVariable Long id, @Valid @ModelAttribute("agentForm") AgentForm agentForm,
                              BindingResult result, Model model, RedirectAttributes flash) {
        AgentFormValidation.checkUnique(agentForm, result, agents);
        if (result.hasErrors()) {
            model.addAttribute("openAgentForm", true);
            return show(id, model);
        }
        agentForm.setDepartmentId(id);
        User agent = agents.create(agentForm);
        flash.addFlashAttribute("success", "Agent " + agent.getFullName() + " created and added to the team.");
        return "redirect:/admin/departments/" + id;
    }

    /** Shrink the team: the agent leaves the department and their unfinished tickets return to its queue. */
    @PostMapping("/{id}/agents/{agentId}/remove")
    public String removeAgent(@PathVariable Long id, @PathVariable Long agentId,
                              @AuthenticationPrincipal AppUserPrincipal admin, RedirectAttributes flash) {
        User agent = agents.get(agentId);
        if (agent.getDepartment() == null || !agent.getDepartment().getId().equals(id)) {
            flash.addFlashAttribute("error", agent.getFullName() + " is not in this department.");
            return "redirect:/admin/departments/" + id;
        }
        int requeued = agents.changeDepartment(agentId, null, admin);
        flash.addFlashAttribute("success", agent.getFullName() + " removed from the department." + requeuedNote(requeued));
        return "redirect:/admin/departments/" + id;
    }

    static String requeuedNote(int requeued) {
        return requeued == 0 ? "" : " " + requeued + " unfinished ticket(s) went back to the queue for reassignment.";
    }
}
