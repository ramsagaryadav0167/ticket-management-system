package com.railbit.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.railbit.Entity.User;
import com.railbit.Service.UserService;

@Controller
public class AuthController {
    private final UserService users;
    public AuthController(UserService users) { this.users = users; }
    @GetMapping("/login") String login() { return "login"; }
    @GetMapping("/signup") String signup(Model model) { model.addAttribute("user", new User()); return "signup"; }
    @PostMapping("/signup") String signup(@ModelAttribute User user, Model model) {
        try { users.registerCustomer(user); return "redirect:/login?registered"; }
        catch (IllegalArgumentException ex) { model.addAttribute("error", ex.getMessage()); model.addAttribute("user", user); return "signup"; }
    }
}
