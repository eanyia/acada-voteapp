-- V1__init.sql - NaijaVote Database Schema and Seed Data

-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create user_roles junction table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Create parties table
CREATE TABLE IF NOT EXISTS parties (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    abbreviation VARCHAR(10) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create votes table
CREATE TABLE IF NOT EXISTS votes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    party_id BIGINT NOT NULL REFERENCES parties(id) ON DELETE RESTRICT,
    voted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_parties_abbreviation ON parties(abbreviation);
CREATE INDEX idx_votes_user_id ON votes(user_id);
CREATE INDEX idx_votes_party_id ON votes(party_id);

-- Seed roles
INSERT INTO roles (name) VALUES ('USER');
INSERT INTO roles (name) VALUES ('ADMIN');

-- Seed users
-- Password: admin123 (BCrypt encoded)
INSERT INTO users (username, email, password_hash, created_at)
VALUES ('admin', 'admin@naijavote.com', '$2b$10$vyhyhHDHQsFnBXm37HrPXuAYK/SDIZDDvsfzuIoblg5uSM0hETDtu', CURRENT_TIMESTAMP);

-- Password: pass123 (BCrypt encoded)
INSERT INTO users (username, email, password_hash, created_at)
VALUES ('user1', 'user1@naijavote.com', '$2b$10$0Jx66YK5bo/lnLdWT0M/kOtHuoSQVQclZKy5ETe7phtuxjXvxEbvW', CURRENT_TIMESTAMP);

INSERT INTO users (username, email, password_hash, created_at)
VALUES ('user2', 'user2@naijavote.com', '$2b$10$I6OukgV7/FZ/txxSQmqgVOQa0j1ONomCTzj45Ujhl1TRc4Syz4ntq', CURRENT_TIMESTAMP);

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id) SELECT id, (SELECT id FROM roles WHERE name = 'ADMIN') FROM users WHERE username = 'admin';
INSERT INTO user_roles (user_id, role_id) SELECT id, (SELECT id FROM roles WHERE name = 'USER') FROM users WHERE username = 'user1';
INSERT INTO user_roles (user_id, role_id) SELECT id, (SELECT id FROM roles WHERE name = 'USER') FROM users WHERE username = 'user2';

-- Seed Nigerian political parties
INSERT INTO parties (name, abbreviation, description, created_at)
VALUES ('All Progressives Congress', 'APC', 'The All Progressives Congress (APC) is a major political party in Nigeria.', CURRENT_TIMESTAMP);

INSERT INTO parties (name, abbreviation, description, created_at)
VALUES ('Peoples Democratic Party', 'PDP', 'The Peoples Democratic Party (PDP) is Nigeria''s largest opposition party.', CURRENT_TIMESTAMP);

INSERT INTO parties (name, abbreviation, description, created_at)
VALUES ('Labour Party', 'LP', 'The Labour Party is a pro-workers and pro-masses political party.', CURRENT_TIMESTAMP);

INSERT INTO parties (name, abbreviation, description, created_at)
VALUES ('New Nigeria Peoples Party', 'NNPP', 'The New Nigeria Peoples Party is focused on good governance and development.', CURRENT_TIMESTAMP);

INSERT INTO parties (name, abbreviation, description, created_at)
VALUES ('Accord Party', 'AP', 'The Accord Party advocates for national unity and development.', CURRENT_TIMESTAMP);
