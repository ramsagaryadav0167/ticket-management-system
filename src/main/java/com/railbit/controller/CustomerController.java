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

import com.railbit.domain.Ticket;
import com.railbit.domain.TicketPriority;
import com.railbit.domain.TicketStatus;
import com.railbit.dto.form.TicketForm;
import com.railbit.security.AppUserPrincipal;
import com.railbit.service.DepartmentService;
import com.railbit.service.TicketService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final TicketService tickets;
    private final DepartmentService departments;
    private final TicketViewHelper view;

    public CustomerController(TicketService tickets, DepartmentService departments, TicketViewHelper view) {
        this.tickets = tickets;
        this.departments = departments;
        this.view = view;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal AppUserPrincipal user, Model model) {
        List<Ticket> mine = tickets.customerTickets(user.getId());
        model.addAttribute("tickets", mine);
        model.addAttribute("openCount", mine.stream().filter(t -> t.getStatus() == TicketStatus.OPEN || t.getStatus().isPending()).count());
        model.addAttribute("resolvedCount", mine.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED).count());
        model.addAttribute("closedCount", mine.stream().filter(t -> t.getStatus() == TicketStatus.CLOSED).count());
        return "customer/dashboard";
    }

    @GetMapping("/tickets/new")
    public String newTicket(Model model) {
        model.addAttribute("form", new TicketForm());
        return formView(model);
    }

    @PostMapping("/tickets")
    public String create(@Valid @ModelAttribute("form") TicketForm form, BindingResult result,
                         @AuthenticationPrincipal AppUserPrincipal user, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            return formView(model);
        }
        Ticket ticket = tickets.create(form, user.getId());
        flash.addFlashAttribute("success", "Ticket #" + ticket.getId() + " submitted. An admin will assign it to an agent shortly.");
        return "redirect:/customer/tickets/" + ticket.getId();
    }

    @GetMapping("/tickets/{id}")
    public String show(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal user, Model model) {
        return view.render(id, user, "/customer", model);
    }

    @PostMapping("/tickets/{id}/messages")
    public String message(@PathVariable Long id, @RequestParam String body, @AuthenticationPrincipal AppUserPrincipal user) {
        tickets.postMessage(id, user, body);
        return "redirect:/customer/tickets/" + id + "#conversation";
    }

    @PostMapping("/tickets/{id}/rate")
    public String rate(@PathVariable Long id, @RequestParam int score, @RequestParam(required = false) String feedback,
                       @AuthenticationPrincipal AppUserPrincipal user, RedirectAttributes flash) {
        tickets.rate(id, user, score, feedback);
        flash.addFlashAttribute("success", "Thanks for your feedback. Ticket closed.");
        return "redirect:/customer/tickets/" + id;
    }

    @PostMapping("/tickets/{id}/reopen")
    public String reopen(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal user, RedirectAttributes flash) {
        tickets.reopen(id, user);
        flash.addFlashAttribute("success", "Ticket reopened and sent back to your agent.");
        return "redirect:/customer/tickets/" + id;
    }

    private String formView(Model model) {
        model.addAttribute("departments", departments.findActive());
        model.addAttribute("priorities", TicketPriority.values());
        return "customer/ticket-form";
    }
}
