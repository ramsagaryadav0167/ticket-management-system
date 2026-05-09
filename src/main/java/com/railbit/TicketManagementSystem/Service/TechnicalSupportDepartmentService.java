package com.railbit.TicketManagementSystem.Service;

import java.util.List;
import com.railbit.TicketManagementSystem.Entity.TechnicalSupportDepartment;

// ✅ This is a service interface for defining operations related to TechnicalSupportDepartment
public interface TechnicalSupportDepartmentService {

    /**
     * ✅ Create and save a new department
     * @param department - the TechnicalSupportDepartment entity to save
     */
    void createDepartment(TechnicalSupportDepartment department);

    /**
     * ✅ Fetch all departments from the database
     * @return List of all TechnicalSupportDepartment entities
     */
    List<TechnicalSupportDepartment> getAllDepartments();

    /**
     * ✅ Delete a department by its ID
     * @param id - the ID of the department to delete
     */
    void deleteDepartmentById(Long id);

    /**
     * ✅ Get a department entity by its ID
     * @param id - department ID
     * @return TechnicalSupportDepartment object
     */
    TechnicalSupportDepartment getDepartmentById(Long id);

    /**
     * ✅ Update an existing department's details
     * @param department - the updated department object
     */
    void updateDepartment(TechnicalSupportDepartment department);
}
