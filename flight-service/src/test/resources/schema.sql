CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    refresh_token VARCHAR(255)
);

CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE airport (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    longitude DECIMAL(9,6) NOT NULL,
    latitude DECIMAL(9,6) NOT NULL,
    description TEXT
);

CREATE TABLE airplane (
    id INT AUTO_INCREMENT PRIMARY KEY,
    model VARCHAR(100) NOT NULL,
    production_date DATE NOT NULL,
    number_of_seats INT NOT NULL,
    max_distance INT NOT NULL,
    airport_id INT NOT NULL,
    FOREIGN KEY (airport_id) REFERENCES airport(id)
);

CREATE TABLE flight (
    id INT AUTO_INCREMENT PRIMARY KEY,
    airplane_id INT NOT NULL,
    start_airport_id INT NOT NULL,
    destination_airport_id INT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    arrival_date TIMESTAMP NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    number_of_available_seats INT NOT NULL,
    description TEXT,
    FOREIGN KEY (airplane_id) REFERENCES airplane(id),
    FOREIGN KEY (start_airport_id) REFERENCES airport(id),
    FOREIGN KEY (destination_airport_id) REFERENCES airport(id)
);
