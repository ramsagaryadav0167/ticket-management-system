package com.railbit.TicketManagementSystem.Controller;

import com.railbit.TicketManagementSystem.Entity.PasswordResetToken;
import com.railbit.TicketManagementSystem.Entity.User;
import com.railbit.TicketManagementSystem.Repository.PasswordResetTokenRepository;
import com.railbit.TicketManagementSystem.Service.EmailService;
import com.railbit.TicketManagementSystem.Service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository tokenRepo;

    @Autowired
    private EmailService emailService;

    // ===== Registration Page =====
    @GetMapping("/register/{role}")
    public String showRegistrationForm(@PathVariable String role, Model model) {
        User user = new User();
        user.setRole("ROLE_" + role.toUpperCase());
        model.addAttribute("user", user);
        return "register";
    }

    @PostMapping("/saveUser")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        try {
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("ROLE_USER");
            }
            userService.registerUser(user);
            return "redirect:/login";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
    }

    // ===== Login Page =====
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) model.addAttribute("error", "Invalid username or password.");
        if (logout != null) model.addAttribute("message", "You have been logged out successfully.");
        return "login";
    }
    
    
    
    
    
    
 // Show forgot-password form
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password"; // Create this Thymeleaf HTML
    }

    // Process form and send reset email
    @PostMapping("/forgot-password")
    public String processForgot(@RequestParam("email") String email, Model model) {
        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isEmpty()) {
            model.addAttribute("error", "No account found with that email.");
            return "forgot-password";
        }

        User user = userOptional.get();
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken(token, user, LocalDateTime.now().plusMinutes(30));
        tokenRepo.save(resetToken);

        emailService.sendResetEmail(email, token);  // Send token link
        model.addAttribute("message", "Reset link sent to your email.");
        return "forgot-password";
    }
    
    
    
 // Show reset form
    @GetMapping("/reset-password")
    public String showResetForm(@RequestParam("token") String token, Model model) {
        PasswordResetToken resetToken = tokenRepo.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Invalid or expired token.");
            return "error";
        }
        model.addAttribute("token", token);
        return "reset-password";  // Create this page
    }

    // Process new password
    @PostMapping("/reset-password")
    public String handleReset(@RequestParam("token") String token,
                              @RequestParam("password") String password,
                              Model model) {
        PasswordResetToken resetToken = tokenRepo.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Invalid or expired token.");
            return "reset-password";
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(password));
        userService.updateUser(user);
        tokenRepo.delete(resetToken);
        return "redirect:/login?resetSuccess";
    }

}