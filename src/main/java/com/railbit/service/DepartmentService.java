package com.railbit.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.railbit.domain.Department;
import com.railbit.domain.Role;
import com.railbit.dto.form.DepartmentForm;
import com.railbit.exception.BusinessException;
import com.railbit.exception.NotFoundException;
import com.railbit.repository.DepartmentRepository;
import com.railbit.repository.TicketRepository;
import com.railbit.repository.UserRepository;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departments;
    private final UserRepository users;
    private final TicketRepository tickets;

    public DepartmentService(DepartmentRepository departments, UserRepository users, TicketRepository tickets) {
        this.departments = departments;
        this.users = users;
        this.tickets = tickets;
    }

    @Transactional(readOnly = true)
    public List<Department> findAll() {
        return departments.findAllByOrderByNameAsc();
    }

    /** Departments customers can raise tickets against. */
    @Transactional(readOnly = true)
    public List<Department> findActive() {
        return departments.findByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Department get(Long id) {
        return departments.findById(id).orElseThrow(() -> new NotFoundException("Department not found"));
    }

    public Department create(DepartmentForm form) {
        String name = form.getName().trim();
        if (departments.existsByNameIgnoreCase(name)) {
            throw new BusinessException("A department named '" + name + "' already exists.");
        }
        Department department = new Department();
        apply(department, form);
        return departments.save(department);
    }

    public Department update(Long id, DepartmentForm form) {
        Department department = get(id);
        String name = form.getName().trim();
        if (departments.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new BusinessException("A department named '" + name + "' already exists.");
        }
        apply(department, form);
        return department;
    }

    /** Inactive departments keep their history but stop accepting new tickets. */
    public Department setActive(Long id, boolean active) {
        Department department = get(id);
        department.setActive(active);
        return department;
    }

    /** Only empty departments can be deleted; otherwise deactivate them to keep history intact. */
    public void delete(Long id) {
        Department department = get(id);
        if (users.countByRoleAndDepartmentId(Role.AGENT, id) > 0) {
            throw new BusinessException("Move or remove this department's agents before deleting it.");
        }
        if (tickets.existsByDepartmentId(id)) {
            throw new BusinessException("This department has tickets. Deactivate it instead of deleting.");
        }
        departments.delete(department);
    }

    private static void apply(Department department, DepartmentForm form) {
        department.setName(form.getName().trim());
        department.setDescription(blankToNull(form.getDescription()));
        department.setContactEmail(blankToNull(form.getContactEmail()));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
