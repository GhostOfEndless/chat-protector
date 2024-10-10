-- Create schema if not exists
CREATE SCHEMA IF NOT EXISTS security;

-- Create table t_app_users
CREATE TABLE security.t_app_users
(
    id                BIGSERIAL PRIMARY KEY,
    c_display_name    VARCHAR(255)        NOT NULL,
    c_login           VARCHAR(255) UNIQUE NOT NULL,
    c_hashed_password VARCHAR(255)        NOT NULL,
    c_role            VARCHAR(50)         NOT NULL
);
