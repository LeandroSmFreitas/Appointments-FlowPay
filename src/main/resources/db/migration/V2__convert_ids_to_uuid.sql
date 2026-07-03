CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE attendances DROP CONSTRAINT IF EXISTS attendances_assigned_agent_id_fkey;
ALTER TABLE attendances DROP CONSTRAINT IF EXISTS attendances_team_id_fkey;
ALTER TABLE agents DROP CONSTRAINT IF EXISTS agents_team_id_fkey;

DROP INDEX IF EXISTS idx_agents_distribution;
DROP INDEX IF EXISTS idx_attendances_waiting_distribution;
DROP INDEX IF EXISTS idx_attendances_assigned_agent;

ALTER TABLE teams ADD COLUMN uuid_id UUID DEFAULT gen_random_uuid();
ALTER TABLE agents ADD COLUMN uuid_id UUID DEFAULT gen_random_uuid();
ALTER TABLE attendances ADD COLUMN uuid_id UUID DEFAULT gen_random_uuid();

ALTER TABLE agents ADD COLUMN team_uuid UUID;
ALTER TABLE attendances ADD COLUMN team_uuid UUID;
ALTER TABLE attendances ADD COLUMN assigned_agent_uuid UUID;

UPDATE agents
SET team_uuid = teams.uuid_id
FROM teams
WHERE agents.team_id = teams.id;

UPDATE attendances
SET team_uuid = teams.uuid_id
FROM teams
WHERE attendances.team_id = teams.id;

UPDATE attendances
SET assigned_agent_uuid = agents.uuid_id
FROM agents
WHERE attendances.assigned_agent_id = agents.id;

ALTER TABLE teams ALTER COLUMN uuid_id SET NOT NULL;
ALTER TABLE agents ALTER COLUMN uuid_id SET NOT NULL;
ALTER TABLE agents ALTER COLUMN team_uuid SET NOT NULL;
ALTER TABLE attendances ALTER COLUMN uuid_id SET NOT NULL;
ALTER TABLE attendances ALTER COLUMN team_uuid SET NOT NULL;

ALTER TABLE attendances DROP CONSTRAINT IF EXISTS attendances_pkey;
ALTER TABLE agents DROP CONSTRAINT IF EXISTS agents_pkey;
ALTER TABLE teams DROP CONSTRAINT IF EXISTS teams_pkey;

ALTER TABLE attendances DROP COLUMN id;
ALTER TABLE attendances DROP COLUMN team_id;
ALTER TABLE attendances DROP COLUMN assigned_agent_id;

ALTER TABLE agents DROP COLUMN id;
ALTER TABLE agents DROP COLUMN team_id;

ALTER TABLE teams DROP COLUMN id;

ALTER TABLE teams RENAME COLUMN uuid_id TO id;
ALTER TABLE agents RENAME COLUMN uuid_id TO id;
ALTER TABLE agents RENAME COLUMN team_uuid TO team_id;
ALTER TABLE attendances RENAME COLUMN uuid_id TO id;
ALTER TABLE attendances RENAME COLUMN team_uuid TO team_id;
ALTER TABLE attendances RENAME COLUMN assigned_agent_uuid TO assigned_agent_id;

ALTER TABLE teams ADD CONSTRAINT teams_pkey PRIMARY KEY (id);
ALTER TABLE agents ADD CONSTRAINT agents_pkey PRIMARY KEY (id);
ALTER TABLE attendances ADD CONSTRAINT attendances_pkey PRIMARY KEY (id);

ALTER TABLE agents
    ADD CONSTRAINT agents_team_id_fkey
    FOREIGN KEY (team_id) REFERENCES teams (id);

ALTER TABLE attendances
    ADD CONSTRAINT attendances_team_id_fkey
    FOREIGN KEY (team_id) REFERENCES teams (id);

ALTER TABLE attendances
    ADD CONSTRAINT attendances_assigned_agent_id_fkey
    FOREIGN KEY (assigned_agent_id) REFERENCES agents (id);

ALTER TABLE teams ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE agents ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE attendances ALTER COLUMN id SET DEFAULT gen_random_uuid();

CREATE INDEX idx_agents_distribution
    ON agents (team_id, status, active_count, last_assigned_at);

CREATE INDEX idx_attendances_waiting_distribution
    ON attendances (team_id, status, created_at);

CREATE INDEX idx_attendances_assigned_agent
    ON attendances (assigned_agent_id);
