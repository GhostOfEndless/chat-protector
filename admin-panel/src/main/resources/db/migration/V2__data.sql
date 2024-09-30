-- noinspection SqlResolveForFile
-- user is user and password is password
INSERT INTO security.t_app_user(c_username, c_full_name, c_hashed_password)
VALUES ('user', 'Aleksey User', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW'),
       ('admin', 'Aleksey Admin', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW');


INSERT INTO security.t_app_user_role(c_app_user_id, c_role) VALUES (1, 'USER'),
                                                                   (2, 'ADMIN');