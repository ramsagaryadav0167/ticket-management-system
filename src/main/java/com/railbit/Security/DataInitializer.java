package com.railbit.Security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.railbit.Entity.User;
import com.railbit.Repository.UserRepository;

@Configuration
public class DataInitializer {
    @Bean CommandLineRunner defaultAdmin(UserRepository users, PasswordEncoder encoder) {
        return args -> { if (users.findByUsername("admin").isEmpty()) {
            User admin = new User(); admin.setUsername("admin"); admin.setPassword(encoder.encode("admin123"));
            admin.setFullName("System Administrator"); admin.setEmail("admin@local.test"); admin.setRole("ROLE_ADMIN"); users.save(admin);
        }};
    }
}
