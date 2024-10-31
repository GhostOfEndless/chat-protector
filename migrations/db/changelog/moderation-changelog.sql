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
CREATE TABLE IF NOT EXISTS moderation.bot_app_user
(
    user_id         BIGINT PRIMARY KEY,
    user_first_name TEXT      NOT NULL,
    user_last_name  TEXT      NOT NULL,
    user_username   TEXT      NOT NULL,
    user_state      TEXT      NOT NULL,
    addition_date   TIMESTAMP NOT NULL
);

--changeset ghostofendless:5
CREATE TABLE IF NOT EXISTS moderation.group_chat
(
    chat_id       BIGINT PRIMARY KEY,
    chat_name     TEXT      NOT NULL,
    addition_date TIMESTAMP NOT NULL
);

--changeset ghostofendless:6
CREATE TABLE IF NOT EXISTS moderation.user_chat_role
(
    user_id       BIGINT,
    chat_id       BIGINT,
    role          TEXT      NOT NULL,
    addition_date TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, chat_id),
    FOREIGN KEY (user_id) REFERENCES moderation.bot_app_user (user_id) ON DELETE CASCADE,
    FOREIGN KEY (chat_id) REFERENCES moderation.group_chat (chat_id) ON DELETE CASCADE
);

--changeset ghostofendless:7
CREATE INDEX idx_user_chat_role_user ON moderation.user_chat_role (user_id);
CREATE INDEX idx_user_chat_role_chat ON moderation.user_chat_role (chat_id);
