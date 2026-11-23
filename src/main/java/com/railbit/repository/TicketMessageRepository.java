package com.railbit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.railbit.domain.TicketMessage;

public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {

    @EntityGraph(attributePaths = "sender")
    List<TicketMessage> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
