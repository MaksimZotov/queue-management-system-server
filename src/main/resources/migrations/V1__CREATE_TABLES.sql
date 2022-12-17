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