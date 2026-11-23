package com.railbit;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.railbit.domain.Ticket;
import com.railbit.domain.TicketStatus;
import com.railbit.domain.User;
import com.railbit.repository.DepartmentRepository;
import com.railbit.repository.TicketRepository;
import com.railbit.repository.UserRepository;
import com.railbit.security.AppUserPrincipal;

/** Renders every screen for every role against the demo data, so template mistakes fail the build. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class PageRenderingTest {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired TicketRepository tickets;
    @Autowired DepartmentRepository departments;

    private AppUserPrincipal as(String username) {
        User user = users.findByUsernameIgnoreCase(username).orElseThrow();
        return new AppUserPrincipal(user);
    }

    @Test
    void publicPagesRender() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(get("/register")).andExpect(status().isOk());
        mvc.perform(get("/admin/dashboard")).andExpect(status().is3xxRedirection());
    }

    @Test
    void adminPagesRender() throws Exception {
        var admin = user(as("admin"));
        Long ticketId = tickets.findAll().get(0).getId();
        Long departmentId = departments.findAllByOrderByNameAsc().get(0).getId();
        Long agentId = users.findByUsernameIgnoreCase("ravi").orElseThrow().getId();

        mvc.perform(get("/admin/dashboard").with(admin)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Unassigned tickets")));
        mvc.perform(get("/admin/tickets").with(admin)).andExpect(status().isOk());
        mvc.perform(get("/admin/tickets").param("status", "OPEN").param("q", "invoice").with(admin)).andExpect(status().isOk());
        mvc.perform(get("/admin/tickets/" + ticketId).with(admin)).andExpect(status().isOk());
        mvc.perform(get("/admin/departments").with(admin)).andExpect(status().isOk());
        mvc.perform(get("/admin/departments/" + departmentId).with(admin)).andExpect(status().isOk());
        mvc.perform(get("/admin/agents").with(admin)).andExpect(status().isOk());
        mvc.perform(get("/admin/agents").param("departmentId", departmentId.toString()).with(admin)).andExpect(status().isOk());
        mvc.perform(get("/admin/agents/" + agentId).with(admin)).andExpect(status().isOk());
    }

    @Test
    void agentPagesRender() throws Exception {
        var ravi = user(as("ravi"));
        Long raviId = users.findByUsernameIgnoreCase("ravi").orElseThrow().getId();
        Ticket mine = tickets.findByAgentIdAndStatusInOrderByUpdatedAtDesc(raviId, TicketStatus.PENDING).get(0);
        mvc.perform(get("/agent/dashboard").with(ravi)).andExpect(status().isOk());
        mvc.perform(get("/agent/dashboard").param("view", "completed").with(ravi)).andExpect(status().isOk());
        mvc.perform(get("/agent/tickets/" + mine.getId()).with(ravi)).andExpect(status().isOk());
    }

    @Test
    void customerPagesRenderAndRolesAreIsolated() throws Exception {
        var alice = user(as("alice"));
        mvc.perform(get("/customer/dashboard").with(alice)).andExpect(status().isOk());
        mvc.perform(get("/customer/tickets/new").with(alice)).andExpect(status().isOk());
        mvc.perform(get("/admin/dashboard").with(alice)).andExpect(status().isForbidden());
        mvc.perform(get("/agent/dashboard").with(alice)).andExpect(status().isForbidden());
    }

    @Test
    void customerCanRaiseTicketAndValidationErrorsReRenderForm() throws Exception {
        var bob = user(as("bob"));
        Long departmentId = departments.findByActiveTrueOrderByNameAsc().get(0).getId();

        mvc.perform(post("/customer/tickets").with(bob).with(csrf())
                        .param("title", "").param("description", "").param("priority", "LOW"))
                .andExpect(status().isOk());

        mvc.perform(post("/customer/tickets").with(bob).with(csrf())
                        .param("title", "Printer jam").param("description", "Paper stuck")
                        .param("departmentId", departmentId.toString()).param("priority", "LOW"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void loginRedirectsByRole() throws Exception {
        mvc.perform(formLogin("/login").user("ravi").password("password123"))
                .andExpect(redirectedUrl("/agent/dashboard"));
    }
}
