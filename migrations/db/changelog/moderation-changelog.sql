--liquibase formatted sql

--changeset ghostofendless:1
CREATE SCHEMA IF NOT EXISTS moderation;

--changeset ghostofendless:2
CREATE SEQUENCE IF NOT EXISTS moderation.deleted_message_seq START WITH 1 INCREMENT BY 10 CACHE 1000;

--changeset ghostofendless:3
CREATE TABLE IF NOT EXISTS moderation.group_chat
(
    id            BIGINT PRIMARY KEY,
    name          TEXT      NOT NULL,
    addition_date TIMESTAMP NOT NULL DEFAULT NOW()
);

--changeset ghostofendless:4
CREATE TABLE IF NOT EXISTS moderation.app_user
(
    id              BIGINT PRIMARY KEY,
    first_name      TEXT      NOT NULL,
    last_name       TEXT      NOT NULL,
    username        TEXT      NOT NULL,
    hashed_password TEXT,
    role            TEXT      NOT NULL DEFAULT 'USER',
    locale          TEXT      NOT NULL DEFAULT 'ru',
    addition_date   TIMESTAMP NOT NULL DEFAULT NOW()
);

--changeset ghostofendless:5
CREATE TABLE IF NOT EXISTS moderation.personal_chat
(
    user_id         BIGINT PRIMARY KEY,
    last_message_id INTEGER NOT NULL,
    state           TEXT    NOT NULL,
    FOREIGN KEY (user_id) REFERENCES moderation.app_user (id) ON DELETE CASCADE
);

--changeset ghostofendless:6
CREATE TABLE IF NOT EXISTS moderation.deleted_text_message
(
    id            BIGINT             DEFAULT nextval('moderation.deleted_message_seq') PRIMARY KEY,
    chat_id       BIGINT    NOT NULL REFERENCES moderation.group_chat (id) ON DELETE CASCADE,
    message_id    INTEGER   NOT NULL,
    message_text  TEXT      NOT NULL,
    user_id       BIGINT REFERENCES moderation.app_user (id) ON DELETE CASCADE,
    deletion_time TIMESTAMP NOT NULL DEFAULT NOW(),
    reason        TEXT      NOT NULL
);
