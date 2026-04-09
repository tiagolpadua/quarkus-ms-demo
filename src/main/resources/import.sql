INSERT INTO pet_user (id, username, firstName, lastName, email, password, phone, userStatus)
VALUES (nextval('user_seq'), 'seed-user-1', 'Seed', 'One', 'seed1@example.com', 'secret', '555-0001', 1);
INSERT INTO pet_user (id, username, firstName, lastName, email, password, phone, userStatus)
VALUES (nextval('user_seq'), 'seed-user-2', 'Seed', 'Two', 'seed2@example.com', 'secret', '555-0002', 1);
