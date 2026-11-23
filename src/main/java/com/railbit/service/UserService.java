package com.railbit.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.railbit.domain.Department;
import com.railbit.domain.Role;
import com.railbit.domain.User;
import com.railbit.dto.form.RegisterForm;
import com.railbit.exception.BusinessException;
import com.railbit.exception.NotFoundException;
import com.railbit.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerCustomer(RegisterForm form) {
        return createAccount(form, Role.CUSTOMER, null);
    }

    /** Shared by customer sign-up, agent creation and seeding. */
    User createAccount(RegisterForm form, Role role, Department department) {
        String username = form.getUsername().trim();
        String email = form.getEmail().trim().toLowerCase();
        if (users.existsByUsernameIgnoreCase(username)) {
            throw new BusinessException("The username '" + username + "' is already taken.");
        }
        if (users.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("An account with this email already exists.");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(form.getFullName().trim());
        user.setPhone(form.getPhone() == null || form.getPhone().isBlank() ? null : form.getPhone().trim());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole(role);
        user.setDepartment(department);
        return users.save(user);
    }

    @Transactional(readOnly = true)
    public User get(Long id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public boolean usernameTaken(String username) {
        return users.existsByUsernameIgnoreCase(username.trim());
    }

    @Transactional(readOnly = true)
    public boolean emailTaken(String email) {
        return users.existsByEmailIgnoreCase(email.trim());
    }
}
