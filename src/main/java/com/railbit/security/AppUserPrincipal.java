package com.railbit.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.railbit.domain.Role;
import com.railbit.domain.User;

/** The logged-in user. Carries the id and display name so pages need no extra lookup. */
public class AppUserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String fullName;
    private final Role role;
    private final boolean enabled;

    public AppUserPrincipal(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.fullName = user.getFullName();
        this.role = user.getRole();
        this.enabled = user.isEnabled();
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public Role getRole() { return role; }
    public boolean isAdmin() { return role == Role.ADMIN; }
    public boolean isAgent() { return role == Role.AGENT; }
    public boolean isCustomer() { return role == Role.CUSTOMER; }

    /** Initials for the avatar badge, e.g. "Jane Doe" -> "JD". */
    public String getInitials() {
        StringBuilder sb = new StringBuilder();
        for (String part : fullName.trim().split("\\s+")) {
            if (!part.isEmpty() && sb.length() < 2) sb.append(Character.toUpperCase(part.charAt(0)));
        }
        return sb.toString();
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.authority()));
    }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isEnabled() { return enabled; }
}
