// Declares the package the controller belongs to
package com.railbit.TicketManagementSystem.Controller;

// Java and Spring imports
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // ✅ Used to pass data to Thymeleaf
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.railbit.TicketManagementSystem.Entity.User;
import com.railbit.TicketManagementSystem.Service.UserService;

// Marks this class as a Spring MVC Controller

@Controller
// All mappings in this controller will start with /profile
@RequestMapping("/user/profile")
public class ProfileController {

    // Injects the UserService to access user-related business logic
    @Autowired
    private UserService userService;

    // ✅ View Profile
    // Maps GET request to /profile
    @GetMapping
    public String viewProfile(Model model, Principal principal) {
        // Get the currently logged-in user's username from the security context
        User user = userService.findByUsername(principal.getName()); // Finds the user by username

        // Adds user object to the model to show on profile page
        model.addAttribute("user", user);
        return "profile"; // Loads profile.html view
    }

    // ✅ Edit Profile
    // Maps GET request to /profile/edit
    @GetMapping("/edit")
    public String editProfile(Model model, Principal principal) {
        // Retrieve current user information
        User user = userService.findByUsername(principal.getName());

        // Add user to model for pre-populating the form fields
        model.addAttribute("user", user);
        return "edit-profile"; // Loads edit-profile.html
    }

    // ✅ Update Profile
    // Handles POST request to update the user's profile info
    @PostMapping("/update")
    public String updateProfile(@ModelAttribute("user") User updatedUser,
                                Principal principal,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        // Update the user profile
        userService.updateProfile(principal.getName(), updatedUser);

        // Flash success message
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");

        // Redirect to profile page
        return "redirect:/user/profile";
    }

}
