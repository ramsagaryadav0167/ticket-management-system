// Package declaration
package com.railbit.TicketManagementSystem.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// JPA annotations
import jakarta.persistence.*;

/**
 * Represents a system user (admin, agent, or customer).
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "user") // Explicit table name (optional but clear)
public class User {

    // -------------------------
    // Primary Key
    // -------------------------

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // -------------------------
    // Core Authentication Fields
    // -------------------------

    private String username;

    private String email;

    private String phone;

    private String password;

    /**
     * User role (e.g., ROLE_ADMIN, ROLE_USER, ROLE_CUSTOMER)
     */
    private String role;

    // -------------------------
    // Profile Details
    // -------------------------

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "gender")
    private String gender;

    @Column(name = "address")
    private String address;

    // -------------------------
    // Department Relationship
    // -------------------------

    /**
     * Many users can be assigned to one technical support department.
     */
    @ManyToOne
    @JoinColumn(name = "technical_support_department_id")
    private TechnicalSupportDepartment technicalSupportDepartment;

    // -------------------------
    // Constructors
    // -------------------------

    public User() {
        // Default constructor
    }

    // -------------------------
    // Getters and Setters
    // -------------------------

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public TechnicalSupportDepartment getTechnicalSupportDepartment() {
        return technicalSupportDepartment;
    }

    public void setTechnicalSupportDepartment(TechnicalSupportDepartment technicalSupportDepartment) {
        this.technicalSupportDepartment = technicalSupportDepartment;
    }

    // -------------------------
    // toString for Debugging
    // -------------------------

    @Override
    public String toString() {
        return "User [id=" + id +
               ", username=" + username +
               ", phone=" + phone +
               ", email=" + email +
               ", password=" + password +
               ", role=" + role + "]";
    }
}
