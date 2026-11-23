package com.railbit.dto.form;

import com.railbit.domain.Department;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DepartmentForm {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @Email
    @Size(max = 150)
    private String contactEmail;

    public static DepartmentForm from(Department d) {
        DepartmentForm form = new DepartmentForm();
        form.setName(d.getName());
        form.setDescription(d.getDescription());
        form.setContactEmail(d.getContactEmail());
        return form;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
}
