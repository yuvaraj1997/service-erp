--liquibase formatted sql

--changeset Yuvaraj:1741060782-1
-- Users table
CREATE TABLE IF NOT EXISTS main.users (
	id SERIAL   PRIMARY KEY,
	email       VARCHAR(255) UNIQUE NOT NULL,
	first_name  VARCHAR(255) NOT NULL,
	last_name   VARCHAR(255) NULL,
	password    VARCHAR(255) NULL,
	status      VARCHAR(50) NOT NULL,
	created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
	updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Roles table
CREATE TABLE IF NOT EXISTS main.roles (
    id SERIAL   PRIMARY KEY,
    name        VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Seed Role Data
INSERT INTO main.roles (name, description)
VALUES
    ('SUPER_ADMIN', 'Super admin role'),
    ('USER', 'Regular user role')
ON CONFLICT (name) DO NOTHING;

-- User-Roles junction table (many-to-many relationship)
CREATE TABLE IF NOT EXISTS main.user_roles (
    user_id     INTEGER NOT NULL,
    role_id     INTEGER NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES main.users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES main.roles(id) ON DELETE CASCADE
);

-- Verification Tokens table (many-to-one relationship)
CREATE TABLE IF NOT EXISTS main.verification_tokens (
    id SERIAL   PRIMARY KEY,
    token       VARCHAR(255) UNIQUE NOT NULL,       -- Unique token (e.g., UUID)
    user_id     INTEGER NOT NULL,                   -- Links to users table
    type        VARCHAR(50) NOT NULL,               -- Purpose of the token (e.g., EMAIL_VERIFICATION, PASSWORD_RESET)
    expiry_date TIMESTAMP NOT NULL,                 -- Token expiration
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    used_at     TIMESTAMP NULL,                     -- Optional: When token was used (for auditing)
    status      VARCHAR(20) DEFAULT 'PENDING',      -- Optional: PENDING, USED, EXPIRED
    FOREIGN KEY (user_id) REFERENCES main.users(id) ON DELETE CASCADE
);


-- User Sessions Table
CREATE TABLE IF NOT EXISTS main.user_sessions (
    id SERIAL   PRIMARY KEY,
    user_id     INTEGER NOT NULL,                   -- Links to users table
    session_id  VARCHAR(255) UNIQUE NOT NULL,       -- Session Id
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,     -- Session active flag
    expiry_date TIMESTAMP NOT NULL,                 -- Session expiration
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES main.users(id) ON DELETE CASCADE
);

-- Add index for user_id to optimize foreign key lookups
CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON main.user_sessions(user_id);

-- Add index for session_id to speed up lookups (already UNIQUE, but an explicit index helps)
CREATE INDEX IF NOT EXISTS idx_user_sessions_session_id ON main.user_sessions(session_id);