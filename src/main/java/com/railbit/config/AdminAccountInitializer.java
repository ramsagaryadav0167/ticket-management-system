package com.railbit.config;

import java.security.SecureRandom;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.railbit.domain.Role;
import com.railbit.domain.User;
import com.railbit.repository.UserRepository;

/**
 * Makes sure an administrator exists on first start. Credentials come from configuration
 * (APP_ADMIN_USERNAME / APP_ADMIN_PASSWORD); if no password is configured a random one is
 * generated and printed once in the log.
 */
@Component
@Order(1)
public class AdminAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;
    private final String email;

    public AdminAccountInitializer(UserRepository users, PasswordEncoder passwordEncoder,
                                   @Value("${app.admin.username}") String username,
                                   @Value("${app.admin.password:}") String password,
                                   @Value("${app.admin.email}") String email) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.countByRole(Role.ADMIN) > 0) {
            return;
        }
        String initialPassword = password;
        if (initialPassword == null || initialPassword.isBlank()) {
            byte[] bytes = new byte[12];
            new SecureRandom().nextBytes(bytes);
            initialPassword = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            log.warn("No APP_ADMIN_PASSWORD set. Generated admin password for '{}': {}  (change it after first login)",
                    username, initialPassword);
        }
        User admin = new User();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(initialPassword));
        admin.setFullName("System Administrator");
        admin.setEmail(email);
        admin.setRole(Role.ADMIN);
        users.save(admin);
        log.info("Created initial admin account '{}'", username);
    }
}
