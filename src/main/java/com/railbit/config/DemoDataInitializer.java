package com.railbit.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.railbit.domain.Department;
import com.railbit.domain.TicketPriority;
import com.railbit.domain.User;
import com.railbit.dto.form.AgentForm;
import com.railbit.dto.form.DepartmentForm;
import com.railbit.dto.form.RegisterForm;
import com.railbit.dto.form.TicketForm;
import com.railbit.repository.DepartmentRepository;
import com.railbit.repository.UserRepository;
import com.railbit.security.AppUserPrincipal;
import com.railbit.service.AgentService;
import com.railbit.service.DepartmentService;
import com.railbit.service.TicketService;
import com.railbit.service.UserService;

/**
 * Sample departments, agents, customers and tickets for local development ("dev" profile only).
 * All demo accounts use the password {@value #PASSWORD}.
 */
@Component
@Profile("dev")
@Order(2)
public class DemoDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);
    static final String PASSWORD = "password123";

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final DepartmentService departments;
    private final AgentService agents;
    private final UserService users;
    private final TicketService tickets;

    public DemoDataInitializer(DepartmentRepository departmentRepository, UserRepository userRepository,
                               DepartmentService departments, AgentService agents,
                               UserService users, TicketService tickets) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.departments = departments;
        this.agents = agents;
        this.users = users;
        this.tickets = tickets;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (departmentRepository.count() > 0) {
            return;
        }
        Department it = department("IT Support", "Hardware, software, network and account access issues", "it@railbit.local");
        Department hr = department("HR", "Leave, payroll queries, onboarding and policies", "hr@railbit.local");
        Department finance = department("Finance", "Invoices, reimbursements and payments", "finance@railbit.local");
        department("Legal", "Contracts, compliance and legal review", "legal@railbit.local");
        department("Operations", "Facilities, logistics and day-to-day operations", "ops@railbit.local");

        User ravi = agent("ravi", "Ravi Kumar", it);
        User sara = agent("sara", "Sara Thomas", it);
        User neha = agent("neha", "Neha Singh", hr);
        User arjun = agent("arjun", "Arjun Mehta", finance);
        agent("maya", "Maya Patel", null);

        User alice = customer("alice", "Alice Fernandes");
        User bob = customer("bob", "Bob Dsouza");

        AppUserPrincipal admin = new AppUserPrincipal(userRepository.findByUsernameIgnoreCase("admin").orElseThrow());
        List<Long> created = new ArrayList<>();
        created.add(ticket(alice, it, "Laptop will not connect to VPN", "Since this morning the VPN client times out.", TicketPriority.HIGH));
        created.add(ticket(bob, it, "Need access to the shared drive", "Please grant access to the Projects drive.", TicketPriority.MEDIUM));
        created.add(ticket(alice, hr, "Leave balance looks wrong", "My leave balance shows 2 days less than expected.", TicketPriority.LOW));
        created.add(ticket(bob, finance, "Reimbursement pending for 3 weeks", "Travel claim submitted on the 1st is still pending.", TicketPriority.URGENT));
        created.add(ticket(alice, it, "Outlook keeps crashing", "Outlook closes when opening attachments.", TicketPriority.MEDIUM));
        ticket(bob, hr, "Update my bank details", "I changed banks and need payroll updated.", TicketPriority.MEDIUM);
        ticket(alice, finance, "Invoice copy request", "Please send a copy of invoice INV-2041.", TicketPriority.LOW);

        tickets.assign(created.get(0), null, ravi.getId(), admin);
        tickets.assign(created.get(1), null, sara.getId(), admin);
        tickets.assign(created.get(2), null, neha.getId(), admin);
        tickets.assign(created.get(3), null, arjun.getId(), admin);
        tickets.assign(created.get(4), null, ravi.getId(), admin);

        AppUserPrincipal raviP = new AppUserPrincipal(ravi);
        tickets.start(created.get(0), raviP);
        tickets.resolve(created.get(0), raviP);
        tickets.rate(created.get(0), new AppUserPrincipal(alice), 5, "Quick fix, thank you!");
        tickets.start(created.get(4), raviP);
        tickets.resolve(created.get(2), new AppUserPrincipal(neha));

        log.info("Demo data loaded. Agents: ravi, sara, neha, arjun, maya; customers: alice, bob; password: {}", PASSWORD);
    }

    private Department department(String name, String description, String email) {
        DepartmentForm form = new DepartmentForm();
        form.setName(name);
        form.setDescription(description);
        form.setContactEmail(email);
        return departments.create(form);
    }

    private User agent(String username, String fullName, Department department) {
        AgentForm form = new AgentForm();
        fill(form, username, fullName);
        form.setDepartmentId(department == null ? null : department.getId());
        return agents.create(form);
    }

    private User customer(String username, String fullName) {
        RegisterForm form = new RegisterForm();
        fill(form, username, fullName);
        return users.registerCustomer(form);
    }

    private Long ticket(User customer, Department department, String title, String description, TicketPriority priority) {
        TicketForm form = new TicketForm();
        form.setTitle(title);
        form.setDescription(description);
        form.setDepartmentId(department.getId());
        form.setPriority(priority);
        return tickets.create(form, customer.getId()).getId();
    }

    private static void fill(RegisterForm form, String username, String fullName) {
        form.setUsername(username);
        form.setFullName(fullName);
        form.setEmail(username + "@railbit.local");
        form.setPassword(PASSWORD);
    }
}
