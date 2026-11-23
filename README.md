# Ticket Management System

Department-wise support desk built with **Spring Boot 3.5, Spring Security, Spring Data JPA, Thymeleaf, Flyway and MySQL**.

Customers raise tickets to a department → admins assign them to an agent of that department (picked by hand
or automatically to the least-loaded agent) → agents work and resolve them → customers rate (close) or reopen them.
Admins grow or shrink each department's team and track every agent's performance: tickets **assigned**, **pending**
and **completed**, plus completion rate and average customer rating.

---

## Features

| Role | What they can do |
|---|---|
| **Customer** | Register, raise a ticket (department + priority), chat with the agent, rate the agent (closes the ticket) or reopen it |
| **Admin** | Dashboard with the unassigned queue, **department-wise assignment** (choose department → agent, or *Auto – least loaded*), reassign / move tickets between departments, close tickets, search all tickets |
| | Departments: create, edit, activate/deactivate, delete (only when empty) |
| | Teams: **add** an existing agent or create a new one in a department, **remove** an agent, move agents between departments, activate/deactivate agents |
| | **Agent performance** for all agents or per department: assigned / pending / completed / completion % / rating |
| **Agent** | Own dashboard with personal performance figures, *Pending* and *Completed* tabs, start / resolve tickets, chat with the customer |

### Ticket lifecycle

```
OPEN ──assign──▶ ASSIGNED ──start──▶ IN_PROGRESS ──resolve──▶ RESOLVED ──rate──▶ CLOSED
 ▲                  │                    │                       │
 └── agent removed / deactivated ────────┘                       └── reopen ──▶ IN_PROGRESS
```

* **Pending** = `ASSIGNED` + `IN_PROGRESS` (work still to do). **Completed** = `RESOLVED` + `CLOSED`.
* **Assigned** = every ticket currently held by the agent (pending + completed).
* When an agent leaves a department or is deactivated, their pending tickets automatically go **back to the
  department queue**, so no customer is left waiting on someone who can no longer help.
* Every change is written to the ticket's **history** (who assigned, reassigned, resolved, …).
* Tickets use optimistic locking: if two admins assign the same ticket at once, the second gets a clear message.

---

## Project structure

```
ticket-management-system/
├── pom.xml
├── Dockerfile · docker-compose.yml · .env.example
└── src
    ├── main
    │   ├── java/com/railbit
    │   │   ├── TicketManagementSystemApplication.java
    │   │   ├── config/        SecurityConfig, HomePaths, AdminAccountInitializer, DemoDataInitializer (dev only)
    │   │   ├── controller/    Auth, Customer, Agent, Admin, AdminDepartment, AdminAgent controllers + view helpers
    │   │   ├── domain/        JPA entities (User, Department, Ticket, TicketMessage, TicketActivity, TicketRating) + enums
    │   │   ├── dto/           AgentPerformance, DepartmentSummary, DashboardStats
    │   │   │   └── form/      Validated form objects (RegisterForm, AgentForm, DepartmentForm, TicketForm)
    │   │   ├── exception/     BusinessException, NotFoundException, GlobalExceptionHandler
    │   │   ├── repository/    Spring Data repositories (grouped count queries for reporting)
    │   │   ├── security/      AppUserPrincipal, AppUserDetailsService
    │   │   └── service/       TicketService, AgentService, DepartmentService, PerformanceService, UserService
    │   └── resources
    │       ├── application.yml          production config (env variables)
    │       ├── application-dev.yml      local dev: in-memory H2 + demo data
    │       ├── db/migration/            Flyway SQL migrations (schema is versioned)
    │       ├── static/css/app.css       single stylesheet
    │       ├── static/js/app.js         confirm dialogs + department→agent filtering
    │       └── templates
    │           ├── layout/              main (sidebar app shell) and plain (login / errors)
    │           ├── fragments/           reusable pieces: badges, stat tiles, assign form, performance table…
    │           ├── auth/ customer/ agent/ admin/ ticket/ error/
    └── test/java/com/railbit            workflow tests + page-rendering tests for every role
```

---

## Running locally (no database needed)

Requires **JDK 17+**.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Open http://localhost:8080. The `dev` profile uses an in-memory database with sample data:

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Agents | `ravi`, `sara` (IT Support), `neha` (HR), `arjun` (Finance), `maya` (no department) | `password123` |
| Customers | `alice`, `bob` | `password123` |

Run the tests:

```bash
./mvnw test
```

## Running in production (MySQL)

1. Create an empty database (e.g. `ticket_db`) and a user for the app. Flyway creates all tables on first start.
2. Set environment variables:

   | Variable | Purpose | Default |
   |---|---|---|
   | `DB_URL` | JDBC URL | `jdbc:mysql://localhost:3306/ticket_db?...` |
   | `DB_USERNAME` / `DB_PASSWORD` | Database credentials | `root` / *(empty)* |
   | `APP_ADMIN_USERNAME` / `APP_ADMIN_PASSWORD` / `APP_ADMIN_EMAIL` | First admin account (created only if no admin exists). If no password is set, a random one is printed once in the log. | `admin` / *(random)* |
   | `COOKIE_SECURE` | `true` when served over HTTPS | `false` |
   | `PORT` | HTTP port | `8080` |

3. Build and run:

   ```bash
   ./mvnw clean package
   java -jar target/ticket-management-system.jar
   ```

   Or with Docker: `cp .env.example .env`, edit it, then `docker compose up --build`.

Health check for load balancers / Kubernetes: `GET /actuator/health` (also `/liveness` and `/readiness`).

### Production notes

* Schema changes go in new Flyway files (`V2__...sql`); Hibernate only **validates** the schema, it never alters it.
* `open-in-view` is off; every page's data is loaded explicitly with entity graphs (no N+1 queries,
  no lazy-loading surprises). Performance figures use grouped count queries, so they stay fast as teams grow.
* CSRF protection, BCrypt passwords, a Content-Security-Policy header, HTTP-only session cookies and generic
  error pages (no stack traces) are enabled.
* Deactivate departments and agents instead of deleting them so history and performance figures stay intact.

## Upgrading from the previous version

The old database tables (`support_tickets`, `users` with `ROLE_*` strings, …) do not match the new schema.
Start with a **new, empty database**. If you need to keep old data, migrate it with a one-off SQL script into the
new tables before going live.
