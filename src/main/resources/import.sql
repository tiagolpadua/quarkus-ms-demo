INSERT INTO pet_user (id, username, firstName, lastName, email, phone, userStatus)
VALUES (nextval('user_seq'), 'seed-user-1', 'Seed', 'One', 'seed1@example.com', '555-0001', 1);
INSERT INTO pet_user (id, username, firstName, lastName, email, phone, userStatus)
VALUES (nextval('user_seq'), 'seed-user-2', 'Seed', 'Two', 'seed2@example.com', '555-0002', 1);
