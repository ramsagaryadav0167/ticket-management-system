package com.railbit.TicketManagementSystem.Service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.railbit.TicketManagementSystem.Entity.TechnicalSupportDepartment;
import com.railbit.TicketManagementSystem.Repository.TechnicalSupportRepository;

// ✅ Marks this class as a Spring service component, making it eligible for component scanning and dependency injection
@Service
public class TechnicalSupportDepartmentServiceImpl implements TechnicalSupportDepartmentService {

    // ✅ Injects the repository used to perform DB operations related to departments
    @Autowired
    private TechnicalSupportRepository departmentRepository;

    // ✅ Save a new department entity to the database
    @Override
    public void createDepartment(TechnicalSupportDepartment department) {
        departmentRepository.save(department); // Spring Data JPA method to save or update entity
    }

    // ✅ Retrieve all departments from the database
    @Override
    public List<TechnicalSupportDepartment> getAllDepartments() {
        return departmentRepository.findAll(); // Returns a list of all departments
    }

    // ✅ Delete a department by its ID
    @Override
    public void deleteDepartmentById(Long id) {
        departmentRepository.deleteById(id); // Deletes department using ID
    }

    // ✅ Find a department by its ID; return null if not found
    @Override
    public TechnicalSupportDepartment getDepartmentById(Long id) {
        return departmentRepository.findById(id).orElse(null); // Uses Optional; returns null if not present
    }

    // ✅ Update an existing department (same as save because JPA handles both create and update with save)
    @Override
    public void updateDepartment(TechnicalSupportDepartment department) {
        departmentRepository.save(department); // acts as update if ID exists
    }


}
