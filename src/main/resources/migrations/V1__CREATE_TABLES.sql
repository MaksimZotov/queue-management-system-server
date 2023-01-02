CREATE sequence hibernate_sequence start 1 increment 1;

CREATE TABLE account (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    email VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL
);

CREATE TABLE registration_code (
    account_id BIGINT PRIMARY KEY REFERENCES account (id),
    code VARCHAR(4) NOT NULL,
);

CREATE TABLE location (
    id BIGSERIAL PRIMARY KEY,
    owner_username BIGINT REFERENCES account (username) NOT NULL UNIQUE,
    name VARCHAR(256) NOT NULL,
    description VARCHAR(2048) NOT NULL
);

CREATE TABLE queue (
    id BIGSERIAL PRIMARY KEY,
    location_id BIGINT REFERENCES location (id) NOT NULL,
    name VARCHAR(256) NOT NULL,
    description VARCHAR(2048) NOT NULL,
);

CREATE TABLE client_in_queue_status (
    queue_id BIGINT REFERENCES queue (id),
    client_email VARCHAR(32) NOT NULL,
    client_first_name VARCHAR(64) NOT NULL,
    client_last_name VARCHAR(64) NOT NULL,
    client_order_number INTEGER,
    PRIMARY KEY (queue_id, client_phone_number)
);