package com.railbit.Service;

import java.util.Collections;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.railbit.Entity.User;
import com.railbit.Repository.UserRepository;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    public UserService(UserRepository users, PasswordEncoder encoder) { this.users = users; this.encoder = encoder; }
    @Override public UserDetails loadUserByUsername(String username) {
        User user = findByUsername(username);
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(user.getRole())));
    }
    public User findByUsername(String username) { return users.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Account not found")); }
    public List<User> agents() { return users.findByRoleOrderByFullNameAsc("ROLE_AGENT"); }
    public User registerCustomer(User user) { return create(user, "ROLE_CUSTOMER"); }
    public User createAgent(User user) { return create(user, "ROLE_AGENT"); }
    private User create(User user, String role) {
        if (users.existsByUsername(user.getUsername())) throw new IllegalArgumentException("This user ID is already in use.");
        if (users.existsByEmail(user.getEmail())) throw new IllegalArgumentException("This email is already in use.");
        user.setRole(role); user.setPassword(encoder.encode(user.getPassword()));
        return users.save(user);
    }
}
