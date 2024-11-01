--liquibase formatted sql

--changeset ghostofendless:1
CREATE SCHEMA IF NOT EXISTS moderation;

--changeset ghostofendless:2
CREATE SEQUENCE IF NOT EXISTS moderation.deleted_message_seq START WITH 1 INCREMENT BY 10 CACHE 1000;

--changeset ghostofendless:3
CREATE TABLE IF NOT EXISTS moderation.deleted_text_message
(
    id             BIGINT DEFAULT nextval('moderation.deleted_message_seq') PRIMARY KEY,
    chat_id        BIGINT  NOT NULL,
    message_id     INTEGER NOT NULL,
    message_text   TEXT    NOT NULL,
    user_id        BIGINT  NOT NULL,
    user_full_name TEXT    NOT NULL,
    user_username  TEXT    NOT NULL,
    reason         TEXT    NOT NULL
);

--changeset ghostofendless:4
CREATE TABLE IF NOT EXISTS moderation.bot_app_admin
(
    id              BIGINT PRIMARY KEY,
    first_name      TEXT      NOT NULL,
    last_name       TEXT      NOT NULL,
    username        TEXT      NOT NULL,
    hashed_password TEXT      NOT NULL,
    state           TEXT      NOT NULL,
    role            TEXT      NOT NULL,
    addition_date        TIMESTAMP NOT NULL
);

--changeset ghostofendless:5
CREATE TABLE IF NOT EXISTS moderation.group_chat
(
    id       BIGINT PRIMARY KEY,
    name     TEXT      NOT NULL,
    addition_date TIMESTAMP NOT NULL
);
