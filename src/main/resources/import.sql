INSERT INTO pet_user (id, username, firstName, lastName, email, phone, userStatus)
VALUES (nextval('user_seq'), 'seed-user-1', 'Seed', 'One', 'seed1@example.com', '555-0001', 1);
INSERT INTO pet_user (id, username, firstName, lastName, email, phone, userStatus)
VALUES (nextval('user_seq'), 'seed-user-2', 'Seed', 'Two', 'seed2@example.com', '555-0002', 1);

INSERT INTO pet (id, category_id, category_name, name, status)
VALUES (nextval('pet_seq'), 1, 'dogs', 'doggie', 'available');
INSERT INTO pet (id, category_id, category_name, name, status)
VALUES (nextval('pet_seq'), 2, 'cats', 'catty', 'pending');

INSERT INTO pet_photo_urls (pet_id, sort_order, photo_url)
VALUES (1, 0, 'https://example.com/doggie.jpg');
INSERT INTO pet_photo_urls (pet_id, sort_order, photo_url)
VALUES (2, 0, 'https://example.com/catty.jpg');

INSERT INTO pet_tags (pet_id, sort_order, tag_id, tag_name)
VALUES (1, 0, 1, 'friendly');
INSERT INTO pet_tags (pet_id, sort_order, tag_id, tag_name)
VALUES (2, 0, 2, 'indoor');

INSERT INTO store_order (id, petId, quantity, shipDate, status, complete)
VALUES (nextval('store_order_seq'), 1, 1, '2026-04-09T10:15:30Z', 'placed', false);
