package com.railbit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.railbit.security.AppUserPrincipal;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String CSP = "default-src 'self'; "
            + "script-src 'self' https://cdn.jsdelivr.net; "
            + "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; "
            + "font-src 'self' https://cdn.jsdelivr.net; "
            + "img-src 'self' data:; "
            + "form-action 'self'; frame-ancestors 'none'";

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/favicon.ico", "/error",
                        "/actuator/health/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/agent/**").hasRole("AGENT")
                .requestMatchers("/customer/**").hasRole("CUSTOMER")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(roleBasedRedirect())
                .failureUrl("/login?error")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .deleteCookies("JSESSIONID"))
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(CSP)));
        return http.build();
    }

    /** Send each role to its own home page after login. */
    private AuthenticationSuccessHandler roleBasedRedirect() {
        return (request, response, authentication) -> {
            AppUserPrincipal user = (AppUserPrincipal) authentication.getPrincipal();
            response.sendRedirect(request.getContextPath() + HomePaths.of(user.getRole()));
        };
    }
}
