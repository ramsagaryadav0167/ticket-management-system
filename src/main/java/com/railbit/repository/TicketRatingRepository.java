package com.railbit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.railbit.domain.TicketRating;

public interface TicketRatingRepository extends JpaRepository<TicketRating, Long> {

    Optional<TicketRating> findByTicketId(Long ticketId);

    boolean existsByTicketId(Long ticketId);

    @Query("select r.agent.id as agentId, avg(r.score) as average, count(r) as total "
            + "from TicketRating r group by r.agent.id")
    List<AgentRating> averageByAgent();

    interface AgentRating {
        Long getAgentId();
        Double getAverage();
        long getTotal();
    }
}
