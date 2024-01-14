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

INSERT INTO roles (name) VALUES ('ROLE_USER'),('ROLE_ADMIN'),('ROLE_AIRPORT_MANAGER');

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

CREATE TABLE airport (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    longitude DECIMAL(9,6) NOT NULL,
    latitude DECIMAL(9,6) NOT NULL,
    description TEXT
);

INSERT INTO airport (name, country, city, longitude, latitude, description)
VALUES  ('John F. Kennedy International Airport', 'United States', 'New York', 40.6413, -73.7781, 'One of the busiest airports in the world.'),
        ('Heathrow Airport', 'United Kingdom', 'London', 51.4700, -0.4543, 'One of the major international airports in London.'),
        ('Charles de Gaulle Airport', 'France', 'Paris', 49.0097, 2.5479, 'The largest international airport in France.'),
        ('Beijing Capital International Airport', 'China', 'Beijing', 40.0799, 116.6031, 'One of the busiest airports in the world located in Beijing.'),
        ('Sydney Airport', 'Australia', 'Sydney', -33.9461, 151.1772, 'The busiest airport in Australia.'),
        ('Los Angeles International Airport', 'United States', 'Los Angeles', 33.9416, -118.4085, 'One of the busiest airports in the United States.'),
        ('Dubai International Airport', 'United Arab Emirates', 'Dubai', 25.2522, 55.3644, 'One of the busiest airports in the world located in Dubai.'),
        ('Tokyo Haneda Airport', 'Japan', 'Tokyo', 35.5494, 139.7798, 'One of the major airports serving the Greater Tokyo Area.'),
        ('Frankfurt Airport', 'Germany', 'Frankfurt', 50.0333, 8.5706, 'One of the busiest airports in Europe located in Frankfurt.'),
        ('Singapore Changi Airport', 'Singapore', 'Singapore', 1.3644, 103.9915, 'One of the busiest airports in the world located in Singapore.');

