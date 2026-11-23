package com.railbit.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.railbit.config.HomePaths;
import com.railbit.dto.form.RegisterForm;
import com.railbit.security.AppUserPrincipal;
import com.railbit.service.UserService;

import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal AppUserPrincipal user) {
        return "redirect:" + HomePaths.of(user.getRole());
    }

    @GetMapping("/login")
    public String login(@AuthenticationPrincipal AppUserPrincipal user) {
        return user != null ? "redirect:" + HomePaths.of(user.getRole()) : "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form, BindingResult result) {
        if (!result.hasFieldErrors("username") && userService.usernameTaken(form.getUsername())) {
            result.rejectValue("username", "taken", "This username is already taken");
        }
        if (!result.hasFieldErrors("email") && userService.emailTaken(form.getEmail())) {
            result.rejectValue("email", "taken", "An account with this email already exists");
        }
        if (result.hasErrors()) {
            return "auth/register";
        }
        userService.registerCustomer(form);
        return "redirect:/login?registered";
    }
}
