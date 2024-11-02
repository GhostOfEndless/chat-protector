--liquibase formatted sql

--changeset ghostofendless:1
INSERT INTO security.t_app_users (c_display_name, c_login, c_hashed_password, c_role)
VALUES ('Admin',
        'admin',
        '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW',
        'ADMIN'),
       ('User',
        'user',
        '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW',
        'USER');

--changeset ghostofendless:2
INSERT INTO moderation.app_user (id, first_name, last_name, username, hashed_password, role, locale, addition_date)
VALUES (1019570683, 'Ghost', 'of Endless', 'ghost_of_endless',
        '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'OWNER', 'ru',
        NOW());