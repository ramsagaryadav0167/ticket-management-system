package com.railbit.TicketManagementSystem.Controller;

import com.railbit.TicketManagementSystem.Entity.ServicesRequest;
import com.railbit.TicketManagementSystem.Entity.User;
import com.railbit.TicketManagementSystem.Repository.UserRepository;
import com.railbit.TicketManagementSystem.Service.ServicesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ServicesController {

    // Inject the service layer to handle business logic for service requests
    @Autowired
    private ServicesService servicesService;

    // Inject user repository to fetch user details
    @Autowired
    private UserRepository userRepository;

    // ✅ Role-based redirection: admin -> /admin/services, user -> /user/services
    @GetMapping("/services")
    public String redirectBasedOnRole(Authentication authentication, Model model) {
        // Check if user is authenticated and has ROLE_ADMIN authority
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/services"; // Admin redirected to admin services page
        } else {
            return showUserServices(model); // Non-admins (users/customers) see user services page
        }
    }

    // ✅ Display user services request page
    @GetMapping("/user/services")
    public String showUserServices(Model model) {
        // Add a new service request object to the model for form binding
        model.addAttribute("serviceRequest", new ServicesRequest());

        // Fetch and add all service requests to the model (can filter by user if needed)
        model.addAttribute("services", servicesService.getAllServiceRequests());

        // Return user services page (Thymeleaf template: services.html)
        return "services";
    }

    // ✅ Display admin services management page
    @GetMapping("/admin/services")
    public String showAdminServices(Model model) {
        // Fetch all service requests from DB
        model.addAttribute("services", servicesService.getAllServiceRequests());

        // Return admin view (Thymeleaf template: admin-services.html)
        return "admin-services";
    }

    // ✅ Handle user service request form submission
    @PostMapping("/services")
    public String submitService(@ModelAttribute("serviceRequest") ServicesRequest serviceRequest,
                                Authentication authentication) {

        // Get current logged-in user's username
        String username = authentication.getName();

        // Fetch user entity from DB based on username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Associate the logged-in user with the service request
        serviceRequest.setUser(user);

        // Save the request to the database
        servicesService.saveServiceRequest(serviceRequest);

        // Redirect user back to the user services page
        return "redirect:/user/services";
    }

    // ✅ Admin deletes a service request by ID
    @PostMapping("/admin/services/delete/{id}")
    public String deleteService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Delete the service request from DB by its ID
        servicesService.deleteById(id);

        // Add flash message to notify successful deletion
        redirectAttributes.addFlashAttribute("success", "Service deleted successfully.");

        // Redirect to admin services page to reflect changes
        return "redirect:/admin/services";
    }
}
