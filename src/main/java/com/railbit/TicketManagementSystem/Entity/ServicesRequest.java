package com.railbit.TicketManagementSystem.Entity;

import jakarta.persistence.*;

@Entity  // Marks this class as a JPA entity (maps to a DB table)
@Table(name = "services_requests")  // Specifies the table name in the database
public class ServicesRequest {

    @Id  // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment strategy for ID
    private Long id;

    private String serviceName;  // Name or title of the service request
    private String description;  // Description/details of the requested service

    // Many service requests can belong to one user
    @ManyToOne(fetch = FetchType.LAZY)  // Loads user lazily when needed (performance optimization)
    @JoinColumn(name = "user_id")       // Foreign key column in services_requests table
    private User user;  // Reference to the user who submitted this service request

    // -------------------------
    // Getters and Setters
    // -------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
