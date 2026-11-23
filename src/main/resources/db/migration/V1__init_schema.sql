-- Initial schema for the Ticket Management System.

CREATE TABLE departments (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100) NOT NULL,
    description   VARCHAR(500),
    contact_email VARCHAR(150),
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_departments_name UNIQUE (name)
);

CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL,
    password      VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    phone         VARCHAR(20),
    role          VARCHAR(20)  NOT NULL,
    department_id BIGINT,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_department FOREIGN KEY (department_id) REFERENCES departments (id)
);
CREATE INDEX idx_users_role_department ON users (role, department_id);

CREATE TABLE tickets (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    title         VARCHAR(150)  NOT NULL,
    description   VARCHAR(4000) NOT NULL,
    status        VARCHAR(20)   NOT NULL,
    priority      VARCHAR(20)   NOT NULL,
    department_id BIGINT        NOT NULL,
    customer_id   BIGINT        NOT NULL,
    agent_id      BIGINT,
    created_at    DATETIME(6)   NOT NULL,
    updated_at    DATETIME(6),
    assigned_at   DATETIME(6),
    resolved_at   DATETIME(6),
    closed_at     DATETIME(6),
    version       BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_tickets_department FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT fk_tickets_customer FOREIGN KEY (customer_id) REFERENCES users (id),
    CONSTRAINT fk_tickets_agent FOREIGN KEY (agent_id) REFERENCES users (id)
);
CREATE INDEX idx_tickets_status ON tickets (status);
CREATE INDEX idx_tickets_agent_status ON tickets (agent_id, status);
CREATE INDEX idx_tickets_department_status ON tickets (department_id, status);
CREATE INDEX idx_tickets_customer ON tickets (customer_id);

CREATE TABLE ticket_messages (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    ticket_id  BIGINT        NOT NULL,
    sender_id  BIGINT        NOT NULL,
    body       VARCHAR(3000) NOT NULL,
    created_at DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_messages_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users (id)
);
CREATE INDEX idx_messages_ticket ON ticket_messages (ticket_id);

CREATE TABLE ticket_activities (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    ticket_id   BIGINT       NOT NULL,
    actor_id    BIGINT,
    description VARCHAR(500) NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_activities_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id) ON DELETE CASCADE,
    CONSTRAINT fk_activities_actor FOREIGN KEY (actor_id) REFERENCES users (id)
);
CREATE INDEX idx_activities_ticket ON ticket_activities (ticket_id);

CREATE TABLE ticket_ratings (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    ticket_id  BIGINT        NOT NULL,
    agent_id   BIGINT        NOT NULL,
    score      INT           NOT NULL,
    feedback   VARCHAR(1000),
    created_at DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_ratings_ticket UNIQUE (ticket_id),
    CONSTRAINT fk_ratings_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id) ON DELETE CASCADE,
    CONSTRAINT fk_ratings_agent FOREIGN KEY (agent_id) REFERENCES users (id),
    CONSTRAINT ck_ratings_score CHECK (score BETWEEN 1 AND 5)
);
CREATE INDEX idx_ratings_agent ON ticket_ratings (agent_id);
