// Package where this repository class is placed
package com.railbit.TicketManagementSystem.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import com.railbit.TicketManagementSystem.Entity.TechnicalSupportDepartment;
import com.railbit.TicketManagementSystem.Entity.Ticket;
import com.railbit.TicketManagementSystem.Entity.User;

@Repository  // ✅ Marks this interface as a Spring Data repository component
public interface CreateTicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket>{
    // Extends JpaRepository to provide built-in CRUD operations on Ticket entity
    // Ticket → entity type, Long → ID type

    // ✅ Custom query method to fetch all tickets assigned to a given department entity
    // Spring Data JPA will automatically generate SQL: SELECT * FROM ticket WHERE technical_support_department = ?
    List<Ticket> findByTechnicalSupportDepartment(TechnicalSupportDepartment department);

    // ✅ Fetch all tickets by department ID (alternative if you only have the ID, not the entity)
    List<Ticket> findByTechnicalSupportDepartmentId(Long deptId);

    // ✅ Fetch all tickets sorted by creation time in descending order
    // Useful for showing most recent tickets at the top
    List<Ticket> findAllByOrderByCreatedAtDesc();

    // ✅ Count tickets by user
    int countByUser(User user);

    // ✅ Count tickets by user and status
    int countByUserAndStatus(User user, String status);
    
    long countByStatus(String status);
    
	long countBySlaBreachedTrue();
	
	List<Ticket>findByUser(User user);
	


	List<Ticket> findByAssignedToIsNotNull();

	List<Ticket> findByAssignedToIsNull();

	List<Ticket> findByAssignedTo(User user);
	
	
	long countByAssignedToIsNotNull();
	long countByAssignedToIsNull();
	long countByStatusIgnoreCase(String status);

    // ✅ Get the latest ticket for a customer
	Optional<Ticket> findTop1ByUserOrderByCreatedAtDesc(User user); // ✅ CORRECT



	Page<Ticket> findByAssignedToIsNotNull(Pageable pageable);
	Page<Ticket> findByAssignedToIsNull(Pageable pageable);

	
    //assign all agent  chat customer
	// CreateTicketRepository.java or TicketRepository.java

	@Query("SELECT DISTINCT t.assignedTo FROM Ticket t WHERE t.user = :customer AND t.assignedTo IS NOT NULL")
	List<User> findDistinctAssignedAgentsByUser(@Param("customer") User customer);
	//user count dashboard 
	int countByAssignedTo(User user);

	int countByAssignedToAndStatus(User user, String status);
	
	//user page pagination
	Page<Ticket> findByUser(User user, Pageable pageable);
	Page<Ticket> findByUserOrAssignedTo(User user, User assignedTo, Pageable pageable);

	
	Page<Ticket> findByAssignedTo(User user, Pageable pageable);
	
	//barchat
	  int countByTechnicalSupportDepartment_IdAndStatus(Long departmentId, String status);

	  //priority
	  int countByAssignedToAndPriority(User assignedTo, String priority);


	 //pagination
	  // Pagination ke saath tickets by department
	    Page<Ticket> findByTechnicalSupportDepartment_Id(Long departmentId, Pageable pageable);

}
