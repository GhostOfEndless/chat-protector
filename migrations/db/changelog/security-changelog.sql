--liquibase formatted sql

--changeset ghostofendless:1
CREATE SCHEMA IF NOT EXISTS security;

--changeset ghostofendless:2
CREATE TABLE security.t_app_users
(
    id                BIGSERIAL PRIMARY KEY,
    c_display_name    TEXT        NOT NULL,
    c_login           TEXT UNIQUE NOT NULL,
    c_hashed_password TEXT        NOT NULL,
    c_role            TEXT        NOT NULL
);