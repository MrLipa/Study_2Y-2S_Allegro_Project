CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255),
    password_salt VARCHAR(255),
    email VARCHAR(255) UNIQUE,
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

INSERT INTO roles (name) VALUES ('ROLE_USER'),('ROLE_ADMIN'),('ROLE_AIRPORT_MANAGER'),('ROLE_AIRPLANE_MANAGER'),('ROLE_FLIGHT_MANAGER');

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

CREATE TABLE airplane (
    id SERIAL PRIMARY KEY,
    model VARCHAR(100) NOT NULL,
    production_date DATE NOT NULL,
    number_of_seats INTEGER NOT NULL,
    max_distance INTEGER NOT NULL,
    airport_id INTEGER NOT NULL REFERENCES airport(id)
);


INSERT INTO airplane (model, production_date, number_of_seats, max_distance, airport_id)
VALUES
('Boeing 747', '1990-01-10', 416, 15000, 1),
('Airbus A380', '2005-04-27', 853, 15700, 2),
('Boeing 777', '1994-06-12', 396, 17400, 3),
('Airbus A320', '2000-07-15', 150, 6100, 4),
('Boeing 737', '1998-05-20', 85, 12300, 5),
('Bombardier CRJ900', '2003-11-30', 76, 3000, 6),
('Embraer 190', '2005-08-12', 100, 4700, 7),
('Boeing 787 Dreamliner', '2011-09-25', 242, 14800, 8),
('Airbus A350', '2014-12-22', 325, 15500, 9),
('Boeing 757', '1995-03-02', 200, 7630, 10),
('Cessna Citation X', '2002-06-17', 12, 6297, 1),
('Airbus A330', '2007-04-27', 290, 13400, 2),
('McDonnell Douglas MD-80', '1985-12-15', 172, 4600, 3);

CREATE TABLE flight (
    id SERIAL PRIMARY KEY,
    airplane_id INTEGER NOT NULL REFERENCES airplane(id),
    start_airport_id INTEGER NOT NULL REFERENCES airport(id),
    destination_airport_id INTEGER NOT NULL REFERENCES airport(id),
    start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    arrival_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    number_of_available_seats INTEGER NOT NULL,
    description TEXT
);

INSERT INTO flight (airplane_id, start_airport_id, destination_airport_id, start_date, arrival_date, price, number_of_available_seats, description)
VALUES
(1, 1, 2, '2024-01-01 08:00:00', '2024-01-01 12:00:00', 500.00, 416, 'Direct flight from New York to London.'),
(2, 3, 4, '2024-01-02 09:30:00', '2024-01-02 15:45:00', 750.00, 853, 'Non-stop from Paris to Beijing.'),
(3, 5, 6, '2024-01-03 07:15:00', '2024-01-03 09:35:00', 300.00, 396, 'Morning flight from Los Angeles to Dubai.'),
(4, 7, 8, '2024-01-04 21:00:00', '2024-01-05 03:00:00', 450.00, 150, 'Overnight flight from Sydney to Tokyo.'),
(5, 10, 3, '2024-02-15 13:30:00', '2024-02-15 18:00:00', 600.00, 85, 'Afternoon flight from Frankfurt to Paris.'),
(6, 2, 5, '2024-03-10 06:45:00', '2024-03-10 09:50:00', 350.00, 76, 'Early morning flight from London to Los Angeles.'),
(7, 9, 1, '2024-04-20 22:15:00', '2024-04-21 05:20:00', 550.00, 100, 'Overnight flight from Singapore to New York.'),
(8, 4, 8, '2024-05-05 10:00:00', '2024-05-05 15:30:00', 700.00, 242, 'Mid-morning flight from Beijing to Tokyo.'),
(9, 6, 7, '2024-06-18 17:00:00', '2024-06-18 21:45:00', 650.00, 325, 'Evening flight from Dubai to Sydney.'),
(10, 8, 2, '2024-07-25 09:15:00', '2024-07-25 14:00:00', 500.00, 200, 'Late morning flight from Tokyo to London.'),
(1, 3, 9, '2024-08-30 08:30:00', '2024-08-30 16:00:00', 800.00, 416, 'Direct flight from Paris to Singapore.'),
(2, 7, 6, '2024-09-15 19:00:00', '2024-09-16 00:30:00', 720.00, 853, 'Evening flight from Sydney to Dubai.'),
(3, 5, 4, '2024-10-22 11:00:00', '2024-10-22 17:20:00', 560.00, 396, 'Midday flight from Los Angeles to Beijing.'),
(4, 1, 10, '2024-11-09 07:45:00', '2024-11-09 13:30:00', 630.00, 150, 'Morning flight from New York to Frankfurt.');
