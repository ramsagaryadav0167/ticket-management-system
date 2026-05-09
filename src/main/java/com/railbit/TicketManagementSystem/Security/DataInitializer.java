// Package declaration for the security configuration
package com.railbit.TicketManagementSystem.Security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.railbit.TicketManagementSystem.Entity.User;
import com.railbit.TicketManagementSystem.Repository.UserRepository;

@Configuration // ✅ Marks this class as a configuration class for Spring
public class DataInitializer {

    // ✅ This bean runs automatically at application startup
    @Bean
    public CommandLineRunner loadData(UserRepository repo, PasswordEncoder encoder) {
        // CommandLineRunner is a Spring Boot interface used to run logic at app startup
        return args -> {
            // ✅ Check if "admin" user already exists
        	if (repo.findByUsername("admin") == null) {
        	    User admin = new User();
        	    admin.setUsername("admin");
        	    admin.setPassword(encoder.encode("admin123"));
        	    admin.setRole("ROLE_ADMIN");
        	    admin.setFullName("System Administrator");
        	    admin.setPhone("9999999999");
        	    admin.setEmail("admin@example.com");
        	    repo.save(admin);
        	}

        	if (repo.findByUsername("user") == null) {
        	    User user = new User();
        	    user.setUsername("user");
        	    user.setPassword(encoder.encode("user123"));
        	    user.setRole("ROLE_USER");
        	    user.setFullName("Default User");
        	    user.setPhone("8888888888");
        	    user.setEmail("user@example.com");
        	    repo.save(user);
        	}
        };
    }
}
