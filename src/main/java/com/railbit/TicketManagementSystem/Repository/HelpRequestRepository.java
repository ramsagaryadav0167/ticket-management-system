// Package where this repository class resides
package com.railbit.TicketManagementSystem.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.railbit.TicketManagementSystem.Entity.HelpRequest;

// ✅ This interface extends JpaRepository to inherit basic CRUD operations for HelpRequest entity
public interface HelpRequestRepository extends JpaRepository<HelpRequest, Long> {
    // No need to write any code here unless you want custom queries.
    // You automatically get: save(), findById(), findAll(), deleteById(), etc.

    // JpaRepository<HelpRequest, Long>:
    // - HelpRequest: the entity this repository manages
    // - Long: the type of the primary key (ID) of HelpRequest
}
