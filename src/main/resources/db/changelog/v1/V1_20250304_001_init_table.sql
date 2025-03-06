--liquibase formatted sql

--changeset Yuvaraj:1741060782-1
-- Users table
CREATE TABLE IF NOT EXISTS main.users (
	id SERIAL PRIMARY KEY,
	email varchar(255) UNIQUE NOT NULL,
	first_name varchar(255) NOT NULL,
	last_name varchar(255) NULL,
	password varchar(255) NULL,
	status varchar(50) NOT NULL,
	created_at timestamp NOT NULL DEFAULT NOW(),
	updated_at timestamp NOT NULL DEFAULT NOW()
);

-- Roles table
CREATE TABLE IF NOT EXISTS main.roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at timestamp NOT NULL DEFAULT NOW()
);

-- Seed Role Data
INSERT INTO main.roles (name, description)
VALUES
    ('SUPER_ADMIN', 'Super admin role'),
    ('USER', 'Regular user role')
ON CONFLICT (name) DO NOTHING;

-- User-Roles junction table (many-to-many relationship)
CREATE TABLE IF NOT EXISTS main.user_roles (
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES main.users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES main.roles(id) ON DELETE CASCADE
);

-- Verification Tokens table (many-to-one relationship)
CREATE TABLE IF NOT EXISTS main.verification_tokens (
    id SERIAL PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL, -- Unique token (e.g., UUID)
    user_id INTEGER NOT NULL,           -- Links to users table
    type VARCHAR(50) NOT NULL,          -- Purpose of the token (e.g., EMAIL_VERIFICATION, PASSWORD_RESET)
    expiry_date TIMESTAMP NOT NULL,     -- Token expiration
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    used_at TIMESTAMP NULL,             -- Optional: When token was used (for auditing)
    status VARCHAR(20) DEFAULT 'PENDING', -- Optional: PENDING, USED, EXPIRED
    FOREIGN KEY (user_id) REFERENCES main.users(id) ON DELETE CASCADE
);