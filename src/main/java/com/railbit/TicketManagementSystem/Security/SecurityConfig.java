package com.railbit.TicketManagementSystem.Security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Inject both services: one for User (admin/user) and one for Customer
    private final UserDetailsService userService;
  

    public SecurityConfig(@Qualifier("userService") UserDetailsService userService) {
        this.userService = userService;
        
    }

    // Custom handler to redirect based on ROLE after login
    @Bean
    public AuthenticationSuccessHandler myAuthenticationSuccessHandler() {
        return new RoleBasedSuccessHandler(); // Ensure this handler checks ROLE_... properly
    }

    // Main security configuration
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeHttpRequests(auth -> auth
                // Public access for registration and static resources
            		.requestMatchers(
            			    "/register/**", "/saveUser",
            			    "/forgot-password", "/reset-password", "/reset-password/**",
            			    "/css/**", "/js/**", "/images/**"
            			).permitAll()


                // Role-based access
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/user/**").hasAuthority("ROLE_USER")
                .requestMatchers("/customer/**").hasAuthority("ROLE_CUSTOMER")

                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")                        // Shared login page
                .loginProcessingUrl("/perform_login")       // Form POST endpoint
                .successHandler(myAuthenticationSuccessHandler()) // Redirect by role
                .failureUrl("/login?error=true")            // Error redirect
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            // Access denied handler - fallback
            .exceptionHandling()
            .accessDeniedPage("/error/403");

        return http.build();
    }

    // BCrypt for secure password encoding
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Combined AuthenticationManager for both User and Customer
    @Bean
    public AuthenticationManager authManager(HttpSecurity http, PasswordEncoder encoder) throws Exception {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(encoder);
        provider.setUserDetailsService(userService);

        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.authenticationProvider(provider);
        return builder.build();
    }
}
