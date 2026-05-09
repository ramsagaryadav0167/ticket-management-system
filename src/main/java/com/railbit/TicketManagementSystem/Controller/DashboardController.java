// Package where the controller resides
package com.railbit.TicketManagementSystem.Controller;

// Importing necessary classes
import com.railbit.TicketManagementSystem.Service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// This annotation marks the class as a Spring MVC Controller
@Controller
public class DashboardController {

    // Injecting the TicketService bean (used if dashboard later needs ticket data)
    @Autowired
    private TicketService ticketService;

    // Maps the root URL ("/") to the dashboard view
    @GetMapping("/")
    public String dashboard() {
        return "dashboard"; // Returns the name of the Thymeleaf template: dashboard.html
    }

}
