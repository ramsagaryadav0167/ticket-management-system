// Defines the package where this repository class resides
package com.railbit.TicketManagementSystem.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.railbit.TicketManagementSystem.Entity.TechnicalSupportDepartment;

// ✅ Repository interface for managing TechnicalSupportDepartment entities
public interface TechnicalSupportRepository extends JpaRepository<TechnicalSupportDepartment, Long> {
    /*
     * This interface extends JpaRepository to inherit full CRUD functionality
     * for the TechnicalSupportDepartment entity.
     *
     * - TechnicalSupportDepartment → the entity class this repository handles.
     * - Long → the data type of the entity's primary key.
     *
     * Spring Data JPA will automatically generate implementation at runtime.
     *
     * You get built-in methods like:
     *   - save()
     *   - findById()
     *   - findAll()
     *   - deleteById()
     *   - count(), existsById(), etc.
     */
	
}
