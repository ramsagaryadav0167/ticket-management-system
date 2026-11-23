package com.railbit.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.railbit.domain.Ticket;
import com.railbit.domain.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @EntityGraph(attributePaths = {"department", "customer", "agent"})
    Optional<Ticket> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"department", "agent"})
    List<Ticket> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @EntityGraph(attributePaths = {"department", "customer"})
    List<Ticket> findByAgentIdAndStatusInOrderByUpdatedAtDesc(Long agentId, Collection<TicketStatus> statuses);

    @EntityGraph(attributePaths = {"department", "customer"})
    List<Ticket> findByStatusOrderByCreatedAtAsc(TicketStatus status);

    List<Ticket> findByAgentIdAndStatusIn(Long agentId, Collection<TicketStatus> statuses);

    @EntityGraph(attributePaths = {"department", "customer", "agent"})
    @Query(value = """
            select t from Ticket t
            where (:status is null or t.status = :status)
              and (:departmentId is null or t.department.id = :departmentId)
              and (:agentId is null or t.agent.id = :agentId)
              and (:q is null or lower(t.title) like lower(concat('%', :q, '%')))
            """,
           countQuery = """
            select count(t) from Ticket t
            where (:status is null or t.status = :status)
              and (:departmentId is null or t.department.id = :departmentId)
              and (:agentId is null or t.agent.id = :agentId)
              and (:q is null or lower(t.title) like lower(concat('%', :q, '%')))
            """)
    Page<Ticket> search(@Param("status") TicketStatus status,
                        @Param("departmentId") Long departmentId,
                        @Param("agentId") Long agentId,
                        @Param("q") String q,
                        Pageable pageable);

    long countByStatus(TicketStatus status);

    long countByAgentIdAndStatusIn(Long agentId, Collection<TicketStatus> statuses);

    boolean existsByDepartmentId(Long departmentId);

    /** One row per (agent, status) – used to build performance figures in a single query. */
    @Query("select t.agent.id as groupId, t.status as status, count(t) as total "
            + "from Ticket t where t.agent is not null group by t.agent.id, t.status")
    List<StatusCount> countGroupedByAgentAndStatus();

    /** One row per (department, status). */
    @Query("select t.department.id as groupId, t.status as status, count(t) as total "
            + "from Ticket t group by t.department.id, t.status")
    List<StatusCount> countGroupedByDepartmentAndStatus();

    interface StatusCount {
        Long getGroupId();
        TicketStatus getStatus();
        long getTotal();
    }
}
