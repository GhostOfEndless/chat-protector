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