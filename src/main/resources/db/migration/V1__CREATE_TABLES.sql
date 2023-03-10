-- Account
CREATE TABLE account (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL
);
CREATE TABLE registration_code (
    email VARCHAR(64) REFERENCES account (email) NOT NULL,
    code INTEGER NOT NULL,
    PRIMARY KEY (email)
);

-- Location
CREATE TABLE location (
    id BIGSERIAL PRIMARY KEY,
    owner_email VARCHAR(64) REFERENCES account (email) NOT NULL,
    name VARCHAR(256) NOT NULL,
    description VARCHAR(2048)
);

-- Service
CREATE TABLE service (
    id BIGSERIAL PRIMARY KEY,
    location_id BIGINT REFERENCES location (id) NOT NULL,
    name VARCHAR(256) NOT NULL,
    description VARCHAR(2048),
    enabled BOOLEAN NOT NULL
);
CREATE TABLE services_sequence (
    id BIGSERIAL PRIMARY KEY,
    location_id BIGINT REFERENCES location (id) NOT NULL,
    name VARCHAR(256) NOT NULL,
    description VARCHAR(2048),
    enabled BOOLEAN NOT NULL
);
CREATE TABLE service_in_services_sequence (
    service_id BIGINT REFERENCES service (id) NOT NULL,
    services_sequence_id BIGINT REFERENCES services_sequence (id) NOT NULL,
    order_number INTEGER NOT NULL,
    PRIMARY KEY (services_sequence_id, service_id)
);
CREATE TABLE specialist (
    id BIGSERIAL PRIMARY KEY,
    location_id BIGINT REFERENCES location (id) NOT NULL,
    name VARCHAR(256) NOT NULL,
    description VARCHAR(2048),
    enabled BOOLEAN NOT NULL
);
CREATE TABLE service_in_specialist (
    service_id BIGINT REFERENCES service (id) NOT NULL,
    specialist_id BIGINT REFERENCES specialist (id) NOT NULL,
    PRIMARY KEY (service_id, specialist_id)
);

-- Client
CREATE TABLE client_status (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE
);
INSERT INTO client_status VALUES
    (1, 'RESERVED'),
    (2, 'CONFIRMED');
CREATE TABLE client (
    id BIGSERIAL PRIMARY KEY,
    location_id BIGINT REFERENCES location (id) NOT NULL,
    email VARCHAR(64),
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    code INTEGER NOT NULL,
    status VARCHAR(64) REFERENCES client_status (name) NOT NULL,
    wait_timestamp TIMESTAMP
);
CREATE TABLE client_to_chosen_service (
    client_id BIGINT REFERENCES client (id) NOT NULL,
    service_id BIGINT REFERENCES service (id) NOT NULL,
    location_id BIGINT REFERENCES location (id) NOT NULL,
    order_number INTEGER NOT NULL,
    PRIMARY KEY (client_id, service_id, location_id)
);

-- Queue
CREATE TABLE queue (
    id BIGSERIAL PRIMARY KEY,
    location_id BIGINT REFERENCES location (id) NOT NULL,
    specialist_id BIGINT REFERENCES specialist (id) NOT NULL,
    name VARCHAR(256) NOT NULL,
    description VARCHAR(2048),
    enabled BOOLEAN NOT NULL,
    client_id BIGINT REFERENCES client (id)
);
CREATE TABLE client_in_queue (
    client_id BIGINT REFERENCES client (id) NOT NULL,
    queue_id BIGINT REFERENCES queue (id) NOT NULL,
    order_number INTEGER,
    code INTEGER NOT NULL,
    UNIQUE (client_id, queue_id, order_number),
    PRIMARY KEY (client_id)
);
CREATE TABLE client_in_queue_to_chosen_service (
    client_id BIGINT REFERENCES client (id) NOT NULL,
    service_id BIGINT REFERENCES service (id) NOT NULL,
    queue_id BIGINT REFERENCES queue (id) NOT NULL,
    location_id BIGINT REFERENCES location (id) NOT NULL,
    PRIMARY KEY (client_id, service_id, queue_id, location_id)
);

-- Rights
CREATE TABLE rights_status (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE
);
INSERT INTO rights_status VALUES
    (1, 'EMPLOYEE'),
    (2, 'ADMINISTRATOR');
CREATE TABLE rights (
    location_id BIGINT REFERENCES location (id) NOT NULL,
    email VARCHAR(64) NOT NULL,
    status VARCHAR(64) REFERENCES rights_status (name) NOT NULL,
    PRIMARY KEY (location_id, email)
);