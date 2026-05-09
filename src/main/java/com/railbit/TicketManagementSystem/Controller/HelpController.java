// Declares the package location of the controller
package com.railbit.TicketManagementSystem.Controller;

// Required Spring Framework and application-specific imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.railbit.TicketManagementSystem.Entity.HelpRequest;
import com.railbit.TicketManagementSystem.Service.HelpRequestService;

// Marks this class as a Spring MVC Controller
@Controller
@RequestMapping("/user")
public class HelpController {

    // Injects the HelpRequestService to handle business logic related to help requests
    @Autowired
    private HelpRequestService helpService;

    // ✅ Display the help form page
    @GetMapping("/help") // Handles GET requests to /help
    public String helpForm(Model model) {
        // Add a new HelpRequest object to the model so the form can bind to it
        model.addAttribute("helpRequest", new HelpRequest());
        return "help"; // Returns the view help.html (located in templates folder)
    }

    // ✅ Handle the help form submission
    @PostMapping("/help") // Handles POST requests to /help
    public String submitHelp(@ModelAttribute("helpRequest") HelpRequest helpRequest) {
        // Save the submitted help request to the database using the service
        helpService.saveRequest(helpRequest);

        // Redirect to the same help page with a success message
        // In HTML you can check for `?success` to display a confirmation message
        return "redirect:/user/help?success";
    }
}
