DO $$ BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = 'zti_project') THEN
    DROP TABLE IF EXISTS zti_project.userMessage;
    DROP TABLE IF EXISTS zti_project.userFlightId;
    DROP TABLE IF EXISTS zti_project.token;
    DROP TABLE IF EXISTS zti_project.user;
    DROP SCHEMA zti_project CASCADE;
END IF;
END $$;

CREATE SCHEMA zti_project;

CREATE TABLE zti_project.user (
                                  userId SERIAL,
                                  firstName VARCHAR(255),
                                  lastName VARCHAR(255),
                                  email VARCHAR(255) UNIQUE,
                                  password VARCHAR(255),
                                  phone VARCHAR(255),
                                  address VARCHAR(255),
                                  image TEXT,
                                  description VARCHAR(500),
                                  CONSTRAINT user_pk PRIMARY KEY (userId)
);

CREATE TABLE zti_project.userMessage (
                                          id SERIAL PRIMARY KEY,
                                          userId INTEGER REFERENCES zti_project.user(userId) ON DELETE CASCADE,
                                          message VARCHAR(255)
);


CREATE TABLE zti_project.userFlightId (
                                         id SERIAL PRIMARY KEY,
                                         userId INTEGER REFERENCES zti_project.user(userId) ON DELETE CASCADE,
                                         flightId INTEGER
);

CREATE TABLE zti_project.token (
                                   id SERIAL PRIMARY KEY,
                                   userId INTEGER REFERENCES zti_project.user(userId) ON DELETE CASCADE,
                                   refreshToken VARCHAR(255)
);

SELECT * FROM zti_project.user LIMIT 100;

INSERT INTO zti_project.user (
    firstName,
    lastName,
    email,
    password,
    phone,
    address,
    image,
    description
) VALUES (
             'Xavier',
             'Venkatanarasimha',
             'xavier@gmail.com',
             '$2b$10$YparQ5ndmjfaTWqpunT4nOd1DuViciOK7Vj6B4WkgOFR3nHmbVGTS',
             '+48 213 769 420',
             'Alwar, India, Rajasthan',
             'EXAMPLE PHOTO',
             'Hi, Im Xavier, your average guy from Earth. You might know me as a meme maker and comedian. I work at a gas station, although for the life of me, I cant pinpoint its location. Just know its somewhere on Earth! Im also a married man, though my wife seems to think I have a few girlfriends on the side. But you know, wives know everything.'
         );

INSERT INTO zti_project.userFlightId (userId, flightId)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (1, 21),
       (1, 13);