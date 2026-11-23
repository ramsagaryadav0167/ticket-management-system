package com.railbit.controller;

import org.springframework.validation.BindingResult;

import com.railbit.dto.form.AgentForm;
import com.railbit.service.AgentService;

/** Field-level uniqueness checks so the admin sees errors next to the right input. */
final class AgentFormValidation {

    private AgentFormValidation() {
    }

    static void checkUnique(AgentForm form, BindingResult result, AgentService agents) {
        if (!result.hasFieldErrors("username") && form.getUsername() != null && agents.usernameTaken(form.getUsername())) {
            result.rejectValue("username", "taken", "This username is already taken");
        }
        if (!result.hasFieldErrors("email") && form.getEmail() != null && agents.emailTaken(form.getEmail())) {
            result.rejectValue("email", "taken", "An account with this email already exists");
        }
    }
}
