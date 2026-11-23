package com.railbit.dto.form;

/** New agent account: the registration fields plus the department to join (optional). */
public class AgentForm extends RegisterForm {

    private Long departmentId;

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
}
