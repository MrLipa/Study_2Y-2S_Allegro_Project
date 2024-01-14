CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    refresh_token VARCHAR(255)
);
INSERT INTO users (username, password, password_salt, email, refresh_token)
VALUES ('admin', '$2a$10$hlbR8xSCnlUsXkKJI4mOf.aXnnfH4KqS/KLms7HdvU9aQjkazMC5O', '$2a$10$Lt5L35m/vToBge4bJBrVl.', 'admin@example.com', NULL);
/*
password: Admin
*/

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO roles (name) VALUES ('ROLE_USER'),('ROLE_ADMIN');

CREATE TABLE user_roles (
    user_id INTEGER NOT NULL REFERENCES users(id),
    role_id INTEGER NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

INSERT INTO user_roles (user_id, role_id)
SELECT users.id, roles.id
FROM users, roles
WHERE users.username = 'admin'
AND roles.name IN ('ROLE_USER', 'ROLE_ADMIN');
