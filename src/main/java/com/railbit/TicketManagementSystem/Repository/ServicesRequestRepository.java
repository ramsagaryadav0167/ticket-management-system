// Package name for repository classes
package com.railbit.TicketManagementSystem.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.railbit.TicketManagementSystem.Entity.ServicesRequest;

// ✅ Repository interface for managing ServicesRequest entities
public interface ServicesRequestRepository extends JpaRepository<ServicesRequest, Long> {
    /*
     * JpaRepository<ServicesRequest, Long> provides:
     * - Built-in CRUD operations (save, findAll, deleteById, etc.)
     * - Paging and sorting capabilities
     * 
     * ServicesRequest → the entity type this repository works on.
     * Long → the data type of the primary key in ServicesRequest.
     * 
     * You do not need to implement anything manually unless you want custom query methods.
     */
}
