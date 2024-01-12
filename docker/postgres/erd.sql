CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

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