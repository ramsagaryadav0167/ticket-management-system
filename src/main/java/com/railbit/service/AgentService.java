package com.railbit.service;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.railbit.domain.Department;
import com.railbit.domain.Role;
import com.railbit.domain.User;
import com.railbit.dto.form.AgentForm;
import com.railbit.exception.NotFoundException;
import com.railbit.repository.UserRepository;
import com.railbit.security.AppUserPrincipal;

/**
 * Agent team management: create agents, add them to / remove them from departments, and
 * activate or deactivate them. Whenever an agent stops being able to work a ticket, their
 * unfinished tickets go back to the department queue for reassignment.
 */
@Service
@Transactional
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    private final UserRepository users;
    private final UserService userService;
    private final DepartmentService departments;
    private final TicketService tickets;

    public AgentService(UserRepository users, UserService userService,
                        DepartmentService departments, TicketService tickets) {
        this.users = users;
        this.userService = userService;
        this.departments = departments;
        this.tickets = tickets;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return users.findByRoleOrderByFullNameAsc(Role.AGENT);
    }

    @Transactional(readOnly = true)
    public List<User> findByDepartment(Long departmentId) {
        return users.findByRoleAndDepartmentIdOrderByFullNameAsc(Role.AGENT, departmentId);
    }

    /** Agents that could be added to the given department (everyone not already in it). */
    @Transactional(readOnly = true)
    public List<User> findNotInDepartment(Long departmentId) {
        return findAll().stream()
                .filter(a -> a.getDepartment() == null || !a.getDepartment().getId().equals(departmentId))
                .toList();
    }

    @Transactional(readOnly = true)
    public User get(Long id) {
        return users.findWithDepartmentById(id)
                .filter(User::isAgent)
                .orElseThrow(() -> new NotFoundException("Agent not found"));
    }

    public User create(AgentForm form) {
        Department department = form.getDepartmentId() == null ? null : departments.get(form.getDepartmentId());
        User agent = userService.createAccount(form, Role.AGENT, department);
        log.info("Agent {} created in department {}", agent.getUsername(),
                department == null ? "-" : department.getName());
        return agent;
    }

    /**
     * Moves an agent to another department, or removes them from their department when
     * {@code departmentId} is null.
     *
     * @return number of unfinished tickets returned to the old department's queue
     */
    public int changeDepartment(Long agentId, Long departmentId, AppUserPrincipal admin) {
        User agent = get(agentId);
        Department current = agent.getDepartment();
        Long currentId = current == null ? null : current.getId();
        if (Objects.equals(currentId, departmentId)) {
            return 0;
        }
        Department target = departmentId == null ? null : departments.get(departmentId);
        int requeued = tickets.requeueAgentTickets(agentId, admin,
                agent.getFullName() + (target == null ? " left " : " moved to " + target.getName() + " from ")
                        + (current == null ? "no department" : current.getName()));
        agent.setDepartment(target);
        return requeued;
    }

    /**
     * Activates or deactivates an agent. Deactivated agents cannot log in or receive tickets.
     *
     * @return number of unfinished tickets returned to the queue (deactivation only)
     */
    public int setEnabled(Long agentId, boolean enabled, AppUserPrincipal admin) {
        User agent = get(agentId);
        if (agent.isEnabled() == enabled) {
            return 0;
        }
        agent.setEnabled(enabled);
        if (enabled) {
            return 0;
        }
        return tickets.requeueAgentTickets(agentId, admin, agent.getFullName() + " was deactivated");
    }

    @Transactional(readOnly = true)
    public boolean usernameTaken(String username) {
        return userService.usernameTaken(username);
    }

    @Transactional(readOnly = true)
    public boolean emailTaken(String email) {
        return userService.emailTaken(email);
    }
}
