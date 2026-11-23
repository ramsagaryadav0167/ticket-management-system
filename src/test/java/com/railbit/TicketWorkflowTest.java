package com.railbit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.railbit.domain.Department;
import com.railbit.domain.Role;
import com.railbit.domain.Ticket;
import com.railbit.domain.TicketPriority;
import com.railbit.domain.TicketStatus;
import com.railbit.domain.User;
import com.railbit.dto.AgentPerformance;
import com.railbit.dto.form.AgentForm;
import com.railbit.dto.form.DepartmentForm;
import com.railbit.dto.form.RegisterForm;
import com.railbit.dto.form.TicketForm;
import com.railbit.exception.BusinessException;
import com.railbit.repository.UserRepository;
import com.railbit.security.AppUserPrincipal;
import com.railbit.service.AgentService;
import com.railbit.service.DepartmentService;
import com.railbit.service.PerformanceService;
import com.railbit.service.TicketService;
import com.railbit.service.UserService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TicketWorkflowTest {

    @Autowired TicketService tickets;
    @Autowired AgentService agents;
    @Autowired DepartmentService departments;
    @Autowired PerformanceService performance;
    @Autowired UserService users;
    @Autowired UserRepository userRepository;

    AppUserPrincipal admin;
    Department it;
    Department hr;
    User customer;

    @BeforeEach
    void setUp() {
        admin = new AppUserPrincipal(userRepository.findByUsernameIgnoreCase("admin").orElseThrow());
        it = department("IT");
        hr = department("HR");
        customer = users.registerCustomer(account("cust", new RegisterForm()));
    }

    @Test
    void autoAssignPicksLeastLoadedActiveAgentOfTheDepartment() {
        User busy = agent("busy", it);
        User free = agent("free", it);
        agent("hrAgent", hr);
        tickets.assign(ticket(it), null, busy.getId(), admin);

        Ticket auto = tickets.assign(ticket(it), null, null, admin);

        assertThat(auto.getAgent().getId()).isEqualTo(free.getId());
        assertThat(auto.getStatus()).isEqualTo(TicketStatus.ASSIGNED);
    }

    @Test
    void cannotAssignToAgentOfAnotherDepartment() {
        User hrAgent = agent("hrAgent", hr);
        Long id = ticket(it);

        assertThatThrownBy(() -> tickets.assign(id, it.getId(), hrAgent.getId(), admin))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not a member");
    }

    @Test
    void assigningToAnotherDepartmentMovesTheTicket() {
        User hrAgent = agent("hrAgent", hr);
        Ticket ticket = tickets.assign(ticket(it), hr.getId(), hrAgent.getId(), admin);

        assertThat(ticket.getDepartment().getId()).isEqualTo(hr.getId());
    }

    @Test
    void autoAssignFailsWhenDepartmentHasNoActiveAgents() {
        Long id = ticket(it);
        assertThatThrownBy(() -> tickets.assign(id, null, null, admin))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no available agent");
    }

    @Test
    void removingAgentFromDepartmentReturnsPendingTicketsToQueue() {
        User agent = agent("ravi", it);
        Long pending = ticket(it);
        Long done = ticket(it);
        tickets.assign(pending, null, agent.getId(), admin);
        tickets.assign(done, null, agent.getId(), admin);
        tickets.resolve(done, new AppUserPrincipal(agent));

        int requeued = agents.changeDepartment(agent.getId(), null, admin);

        assertThat(requeued).isEqualTo(1);
        assertThat(tickets.queue()).extracting(Ticket::getId).containsExactly(pending);
        assertThat(agents.get(agent.getId()).getDepartment()).isNull();
    }

    @Test
    void deactivatingAgentReturnsPendingTicketsAndExcludesThemFromAutoAssign() {
        User agent = agent("ravi", it);
        Long id = ticket(it);
        tickets.assign(id, null, agent.getId(), admin);

        agents.setEnabled(agent.getId(), false, admin);

        assertThat(tickets.queue()).extracting(Ticket::getId).containsExactly(id);
        assertThatThrownBy(() -> tickets.assign(id, null, null, admin)).isInstanceOf(BusinessException.class);
    }

    @Test
    void performanceCountsAssignedPendingCompletedAndRating() {
        User agent = agent("ravi", it);
        AppUserPrincipal agentP = new AppUserPrincipal(agent);
        Long a = ticket(it), b = ticket(it), c = ticket(it);
        for (Long id : new Long[] {a, b, c}) {
            tickets.assign(id, null, agent.getId(), admin);
        }
        tickets.start(a, agentP);
        tickets.resolve(b, agentP);
        tickets.resolve(c, agentP);
        tickets.rate(c, new AppUserPrincipal(customer), 4, "good");

        AgentPerformance p = performance.forAgent(agents.get(agent.getId()));

        assertThat(p.assigned()).isEqualTo(3);
        assertThat(p.pending()).isEqualTo(1);
        assertThat(p.completed()).isEqualTo(2);
        assertThat(p.completionRate()).isEqualTo(67);
        assertThat(p.averageRating()).isEqualTo(4.0);
        assertThat(performance.departmentSummary(it.getId()).agentCount()).isEqualTo(1);
    }

    @Test
    void customerCannotSeeSomeoneElsesTicket() {
        Long id = ticket(it);
        User other = users.registerCustomer(account("other", new RegisterForm()));

        assertThatThrownBy(() -> tickets.getForView(id, new AppUserPrincipal(other)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void customerCanReopenResolvedTicket() {
        User agent = agent("ravi", it);
        Long id = ticket(it);
        tickets.assign(id, null, agent.getId(), admin);
        tickets.resolve(id, new AppUserPrincipal(agent));

        tickets.reopen(id, new AppUserPrincipal(customer));

        assertThat(tickets.getForView(id, admin).getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
    }

    @Test
    void departmentWithAgentsCannotBeDeleted() {
        agent("ravi", it);
        assertThatThrownBy(() -> departments.delete(it.getId())).isInstanceOf(BusinessException.class);
    }

    // ---------------------------------------------------------------- helpers

    private Department department(String name) {
        DepartmentForm form = new DepartmentForm();
        form.setName(name);
        return departments.create(form);
    }

    private User agent(String username, Department department) {
        AgentForm form = account(username, new AgentForm());
        form.setDepartmentId(department.getId());
        User agent = agents.create(form);
        assertThat(agent.getRole()).isEqualTo(Role.AGENT);
        return agent;
    }

    private Long ticket(Department department) {
        TicketForm form = new TicketForm();
        form.setTitle("Problem");
        form.setDescription("Details");
        form.setDepartmentId(department.getId());
        form.setPriority(TicketPriority.MEDIUM);
        return tickets.create(form, customer.getId()).getId();
    }

    private static <T extends RegisterForm> T account(String username, T form) {
        form.setUsername(username);
        form.setFullName(username + " Test");
        form.setEmail(username + "@example.com");
        form.setPassword("password123");
        return form;
    }
}
