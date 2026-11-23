package com.railbit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.railbit.domain.TicketActivity;

public interface TicketActivityRepository extends JpaRepository<TicketActivity, Long> {

    @EntityGraph(attributePaths = "actor")
    List<TicketActivity> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
