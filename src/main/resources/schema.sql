CREATE TABLE chatrooms (id INT PRIMARY KEY, name VARCHAR(255));
CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(255));
CREATE TABLE chatroom_users (chatroom_id INT, user_id INT, FOREIGN KEY (chatroom_id) REFERENCES chatrooms(id), FOREIGN KEY (user_id) REFERENCES users(id));