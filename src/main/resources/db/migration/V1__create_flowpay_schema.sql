CREATE TABLE teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE,
    CONSTRAINT ck_teams_name CHECK (name IN ('CARTOES', 'EMPRESTIMOS', 'OUTROS'))
);

CREATE TABLE agents (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    team_id BIGINT NOT NULL REFERENCES teams (id),
    status VARCHAR(20) NOT NULL,
    active_count INTEGER NOT NULL DEFAULT 0,
    last_assigned_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_agents_status CHECK (status IN ('ONLINE', 'OFFLINE', 'PAUSED')),
    CONSTRAINT ck_agents_active_count CHECK (active_count BETWEEN 0 AND 3)
);

CREATE TABLE attendances (
    id BIGSERIAL PRIMARY KEY,
    customer_name VARCHAR(120) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    team_id BIGINT NOT NULL REFERENCES teams (id),
    status VARCHAR(20) NOT NULL,
    assigned_agent_id BIGINT REFERENCES agents (id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_attendances_status CHECK (status IN ('WAITING', 'IN_PROGRESS', 'FINISHED', 'CANCELLED'))
);

CREATE INDEX idx_agents_distribution
    ON agents (team_id, status, active_count, last_assigned_at);

CREATE INDEX idx_attendances_waiting_distribution
    ON attendances (team_id, status, created_at);

CREATE INDEX idx_attendances_assigned_agent
    ON attendances (assigned_agent_id);

INSERT INTO teams (name)
VALUES ('CARTOES'),
       ('EMPRESTIMOS'),
       ('OUTROS');
