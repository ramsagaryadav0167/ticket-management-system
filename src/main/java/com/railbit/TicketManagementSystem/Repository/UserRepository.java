// Package declaration for the repository class
package com.railbit.TicketManagementSystem.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.railbit.TicketManagementSystem.Entity.User;

// ✅ Repository interface for managing User entities
public interface UserRepository extends JpaRepository<User, Long> {
    /*
     * JpaRepository<User, Long> provides built-in CRUD methods:
     * - save(), findById(), findAll(), deleteById(), etc.
     *
     * User → The entity this repository handles
     * Long → The type of the primary key in the User entity
     */

    // ✅ Custom method to find a User by their username
    // Spring Data JPA automatically implements this using the method name
	Optional<User> findByUsername(String username);

	

    /*
     * Returns an Optional<User> because the user may or may not exist.
     * This is helpful for login and authentication processes.
     */
	
	List<User> findByRole(String role);   
	
	long countByRole(String role);
	
	
	
	 Optional<User> findFirstByRole(String role);
	
	// UserRepository.java
	 List<User> findByRoleIn(List<String> roles);

	 Optional<User> findByEmail(String email);

}
