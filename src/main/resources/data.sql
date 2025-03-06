CREATE TABLE chatrooms (id INT PRIMARY KEY, name VARCHAR(255));
CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(255));
CREATE TABLE chatroom_users (chatroom_id INT, user_id INT, FOREIGN KEY (chatroom_id) REFERENCES chatrooms(id), FOREIGN KEY (user_id) REFERENCES users(id));

INSERT INTO chatrooms VALUES (1, 'chat-1');
INSERT INTO chatrooms VALUES (2, 'chat-2');
INSERT INTO chatrooms VALUES (3, 'chat-3');

INSERT INTO users VALUES(1, 'user-1');
INSERT INTO users VALUES(2, 'user-2');
INSERT INTO users VALUES(3, 'user-3');
INSERT INTO users VALUES(4, 'user-4');
INSERT INTO users VALUES(5, 'user-5');

INSERT INTO chatroom_users (1, 1);
INSERT INTO chatroom_users (1, 2);
INSERT INTO chatroom_users (1, 3);
INSERT INTO chatroom_users (2, 2);
INSERT INTO chatroom_users (2, 3);
INSERT INTO chatroom_users (2, 4);
INSERT INTO chatroom_users (3, 3);
INSERT INTO chatroom_users (3, 4);
INSERT INTO chatroom_users (3, 5);
