CREATE SCHEMA IF NOT EXISTS security;

CREATE TABLE security.t_app_user
(
    id                BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    c_username        VARCHAR(255) NOT NULL UNIQUE,
    c_full_name            VARCHAR(255) NOT NULL,
    c_hashed_password VARCHAR(255) NOT NULL
);

CREATE TABLE security.t_app_user_role
(
    c_app_user_id BIGINT              NOT NULL REFERENCES security.t_app_user (id),
    c_role        VARCHAR(255) UNIQUE NOT NULL
);