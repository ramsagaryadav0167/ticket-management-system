package com.railbit.TicketManagementSystem.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.railbit.TicketManagementSystem.Entity.User;
import com.railbit.TicketManagementSystem.Repository.UserRepository;

import jakarta.transaction.Transactional;

@Service("userService")
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    // ─────────────────────────────────────
    // 🔐 Spring Security Authentication
    // ─────────────────────────────────────

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }

    // ─────────────────────────────────────
    // 👤 Registration & Profile Update
    // ─────────────────────────────────────

    public void registerUser(User user) {
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public void updateProfile(String username, User updatedUser) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            existingUser.setFullName(updatedUser.getFullName());
            existingUser.setPhone(updatedUser.getPhone());
            existingUser.setGender(updatedUser.getGender());
            existingUser.setAddress(updatedUser.getAddress());
            userRepository.save(existingUser);
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }

    public void save(User user) {
        userRepository.save(user);
    }

    // ─────────────────────────────────────
    // 🔍 User Retrieval
    // ─────────────────────────────────────

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    // ─────────────────────────────────────
    // 🧮 Count Operations
    // ─────────────────────────────────────

    public Long countByRole(String role) {
        return userRepository.countByRole(role);
    }
    // Chat Support
    public List<User> getAllUsersExceptRole(String excludedRole) {
        return userRepository.findAll()
            .stream()
            .filter(u -> !u.getRole().equals(excludedRole))
            .toList();
    }
    
    public User getAdminUser() {
        return userRepository.findFirstByRole("ROLE_ADMIN")
               .orElseThrow(() -> new RuntimeException("Admin not found"));
    }

 // UserService.java
    public List<User> getAllAgentsAndCustomers() {
        return userRepository.findByRoleIn(List.of("ROLE_USER", "ROLE_CUSTOMER"));
    }
    
    @Transactional
    public void updatePasswordByUsername(String username, String encodedPassword) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setPassword(encodedPassword);
        userRepository.save(user);  // This will now ensure update
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void updateUser(User user) {
        userRepository.save(user); // ✅ save() works for both insert and update
    }
    
    //notification service
    public User findAdmin() {
    	return userRepository.findByRole("ROLE_ADMIN").stream().findFirst().orElse(null);
    }
    
}
