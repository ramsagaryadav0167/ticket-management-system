package com.railbit.dto.form;

import com.railbit.domain.TicketPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TicketForm {

    @NotBlank
    @Size(max = 150)
    private String title;

    @NotBlank
    @Size(max = 4000)
    private String description;

    @NotNull(message = "Please choose a department")
    private Long departmentId;

    @NotNull
    private TicketPriority priority = TicketPriority.MEDIUM;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }
}
