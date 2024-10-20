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