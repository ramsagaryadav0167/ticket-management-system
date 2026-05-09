package com.railbit.TicketManagementSystem.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a support ticket in the Ticket Management System.
 */
@Entity
@Table(name = "ticket")
public class Ticket {

    // -----------------------
    // Primary Key
    // -----------------------

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -----------------------
    // Basic Ticket Details
    // -----------------------

    private String title;

    @Column(length = 1000)
    private String description;

    
    

    private String status;

    private String priority;

    private LocalDateTime createdAt;

    private LocalDateTime slaDeadline;

    private boolean slaBreached;
    
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Mobile number must start with 6, 7, 8, or 9 and be 10 digits")
    private String mobileNo;

    @Column(name = "file_upload", length = 255)
    private String fileUpload;

    @Column(name = "feedback", length = 255)
    private String feedback;
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketHistory> historyList = new ArrayList<>();

    

    // -----------------------
    // User Relationships
    // -----------------------

    /**
     * The user who created the ticket.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
   
    /**
     * The user who assigned the ticket.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    /**
     * The user to whom the ticket is currently assigned.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to", nullable = true)
    private User assignedTo;

    @Column(name = "assigned_date")
    private LocalDateTime assignedDate;

    // -----------------------
    // Department Reference
    // -----------------------

    /**
     * The department responsible for handling this ticket.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private TechnicalSupportDepartment technicalSupportDepartment;

    // -----------------------
    // Constructors
    // -----------------------

    public Ticket() {
        // Default constructor for JPA
    }

    public Ticket(Long id, String title, String description, String status, String priority, User user,
                  LocalDateTime createdAt, LocalDateTime slaDeadline, boolean slaBreached,
                  TechnicalSupportDepartment technicalSupportDepartment, String fileUpload, String feedback,
                  String mobileNo) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.user = user;
        this.createdAt = createdAt;
        this.slaDeadline = slaDeadline;
        this.slaBreached = slaBreached;
        this.technicalSupportDepartment = technicalSupportDepartment;
        this.fileUpload = fileUpload;
        this.feedback = feedback;
        this.mobileNo=mobileNo;
    }

    // -----------------------
    // Getters and Setters
    // -----------------------
    

    public Long getId() {
        return id;
    }

    public List<TicketHistory> getHistoryList() {
		return historyList;
	}

	public void setHistoryList(List<TicketHistory> historyList) {
		this.historyList = historyList;
	}

	public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(User assignedBy) {
        this.assignedBy = assignedBy;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDateTime assignedDate) {
        this.assignedDate = assignedDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSlaDeadline() {
        return slaDeadline;
    }

    public void setSlaDeadline(LocalDateTime slaDeadline) {
        this.slaDeadline = slaDeadline;
    }

    public boolean isSlaBreached() {
        return slaBreached;
    }

    public void setSlaBreached(boolean slaBreached) {
        this.slaBreached = slaBreached;
    }

    public TechnicalSupportDepartment getTechnicalSupportDepartment() {
        return technicalSupportDepartment;
    }

    public void setTechnicalSupportDepartment(TechnicalSupportDepartment technicalSupportDepartment) {
        this.technicalSupportDepartment = technicalSupportDepartment;
    }

    public String getFileUpload() {
        return fileUpload;
    }

    public void setFileUpload(String fileUpload) {
        this.fileUpload = fileUpload;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

}
