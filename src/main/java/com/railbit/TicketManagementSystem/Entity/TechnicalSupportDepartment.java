// Package for the entity
package com.railbit.TicketManagementSystem.Entity;

// JPA annotations for ORM mapping
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


// Marks this class as a JPA Entity mapped to a database table
@Entity
public class TechnicalSupportDepartment {

    // Primary key with auto-increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Name of the department
    @NotBlank(message = "Department name is required.")
    @Size(max = 100, message = "Department name must be at most 100 characters.")
    private String departmentName;

    // Description of department duties or scope

    @Size(max = 250, message = "Description must be at most 250 characters.")
    private String description;

    // Contact email for the department
    @Email(message = "Invalid email format.")
    private String contactEmail;

    // One-to-many relationship with Ticket entity (one department can have many tickets)
    // The mappedBy refers to the field name in Ticket class that owns the relationship
    @OneToMany(mappedBy = "technicalSupportDepartment")
    private List<Ticket> ticket = new ArrayList<>();

    // ✅ Default constructor
    public TechnicalSupportDepartment() {
        // Optional default value for departmentName
        this.departmentName = "TechnicalSupport";
    }

    // ✅ Parameterized constructor
    public TechnicalSupportDepartment(String departmentName, String description, String contactEmail) {
        this.departmentName = departmentName;
        this.description = description;
        this.contactEmail = contactEmail;
    }

    // ✅ Getters and setters for each field (needed by Spring and JPA)

    public Long getId() {
        return id;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public List<Ticket> getTicket() {
        return ticket;
    }

    public void setTicket(List<Ticket> ticket) {
        this.ticket = ticket;
    }

    // ✅ toString method for logging/debugging
    @Override
    public String toString() {
        return "TechnicalSupportDepartment{" +
                "id=" + id +
                ", departmentName='" + departmentName + '\'' +
                ", description='" + description + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                '}';
    }
}
