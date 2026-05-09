package com.railbit.TicketManagementSystem.Security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class RoleBasedSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String redirectURL = "/login?error=true"; // fallback URL

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();

            switch (role) {
                case "ROLE_ADMIN":
                    redirectURL = "/admin/dashboard";
                    break;
                case "ROLE_USER":
                    redirectURL = "/user/dashboard";
                    break;
                case "ROLE_CUSTOMER":
                    redirectURL = "/customer/dashboard";
                    break;
            }

            // Break early once matched
            if (!redirectURL.equals("/login?error=true")) {
                break;
            }
        }

        response.sendRedirect(redirectURL);
    }
}
