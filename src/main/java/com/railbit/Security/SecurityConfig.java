package com.railbit.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/signup", "/css/**", "/images/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/agent/**").hasRole("AGENT")
                .requestMatchers("/customer/**").hasRole("CUSTOMER")
                .anyRequest().authenticated())
            .formLogin(login -> login.loginPage("/login").loginProcessingUrl("/perform_login").successHandler((request, response, authentication) -> {
                String role = authentication.getAuthorities().iterator().next().getAuthority();
                response.sendRedirect("ROLE_ADMIN".equals(role) ? "/admin/dashboard" : "ROLE_AGENT".equals(role) ? "/agent/dashboard" : "/customer/dashboard");
            }).permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login?logout")).build();
    }
}
