-- Account
CREATE TABLE account (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    email VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL
);
CREATE TABLE registration_code (
    username VARCHAR(64) PRIMARY KEY UNIQUE,
    code VARCHAR(4) NOT NULL
);

-- Location
CREATE TABLE location (
    id BIGSERIAL PRIMARY KEY,
    owner_username VARCHAR(64) REFERENCES account (username) NOT NULL,
    name VARCHAR(256) NOT NULL,
    description VARCHAR(2048),
    max_columns INTEGER NOT NULL,
    UNIQUE (owner_username, name)
);

-- Service
CREATE TABLE service (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    description VARCHAR(2048),
    supposed_duration BIGINT NOT NULL,
    max_duration BIGINT NOT NULL
);
CREATE TABLE service_in_location (
    service_id BIGINT REFERENCES service (id) NOT NULL,
    location_id BIGINT REFERENCES location (id) NOT NULL,
    PRIMARY KEY (service_id, location_id)
);
CREATE TABLE services_sequence (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    description VARCHAR(2048)
);
CREATE TABLE services_sequence_in_location (
    services_sequence_id BIGINT REFERENCES services_sequence (id) NOT NULL,
    location_id BIGINT REFERENCES location (id) NOT NULL,
    PRIMARY KEY (services_sequence_id, location_id)
);
CREATE TABLE services_in_services_sequence (
    services_sequence_id BIGINT REFERENCES services_sequence (id) NOT NULL,
    service_id BIGINT REFERENCES service (id) NOT NULL,
    order_number INTEGER NOT NULL,
    PRIMARY KEY (services_sequence_id, service_id)
);

-- QueueType
CREATE TABLE queue_type (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    description VARCHAR(2048)
);
CREATE TABLE queue_type_in_location (
    queue_type_id BIGINT REFERENCES queue_type (id) NOT NULL,
    location_id BIGINT REFERENCES location (id) NOT NULL,
    PRIMARY KEY (queue_type_id, location_id)
);
CREATE TABLE service_in_queue_type (
    service_id BIGINT REFERENCES service (id) NOT NULL,
    queue_type_id BIGINT REFERENCES queue_type (id) NOT NULL,
    PRIMARY KEY (service_id, queue_type_id)
);

-- Queue
CREATE TABLE queue (
    id BIGSERIAL PRIMARY KEY,
    queue_type_id BIGINT REFERENCES queue_type (id) NOT NULL,
    location_id BIGINT REFERENCES location (id) NOT NULL,
    name VARCHAR(256) NOT NULL,
    description VARCHAR(2048),
    paused BOOLEAN NOT NULL,
    UNIQUE (location_id, name)
);

-- Client
CREATE TABLE client_status (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE
);
INSERT INTO client_status VALUES
    (1, 'RESERVED'),
    (2, 'CONFIRMED'),
    (3, 'LATE');
CREATE TABLE client (
    id BIGSERIAL PRIMARY KEY,
    location_id BIGINT REFERENCES location (id) NOT NULL,
    email VARCHAR(64),
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    access_key VARCHAR(4) NOT NULL,
    status VARCHAR(64) REFERENCES client_status (name) NOT NULL
);
CREATE TABLE client_code (
    client_id BIGINT REFERENCES client (id) NOT NULL,
    email VARCHAR(64) NOT NULL,
    code VARCHAR(4) NOT NULL,
    PRIMARY KEY (client_id, email)
);
CREATE TABLE client_in_queue (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES client (id) NOT NULL,
    queue_id BIGINT REFERENCES queue (id) NOT NULL,
    order_number INTEGER,
    public_code INTEGER NOT NULL,
    UNIQUE (queue_id, order_number)
);
CREATE TABLE client_to_chosen_service (
    client_id BIGINT REFERENCES client (id) NOT NULL,
    service_id BIGINT REFERENCES service (id) NOT NULL,
    order_number INTEGER NOT NULL,
    queue_is_known BOOLEAN NOT NULL,
    PRIMARY KEY (client_id, service_id)
);
CREATE TABLE client_in_queue_to_chosen_service (
    client_in_queue_id BIGINT REFERENCES client_in_queue (id) NOT NULL,
    service_id BIGINT REFERENCES service (id) NOT NULL,
    queue_id BIGINT REFERENCES queue (id) NOT NULL,
    PRIMARY KEY (client_in_queue_id, service_id, queue_id)
);

-- Rights
CREATE TABLE rights (
    location_id BIGINT REFERENCES location (id) NOT NULL,
    email VARCHAR(64) REFERENCES account (email) NOT NULL,
    PRIMARY KEY (location_id, email)
);

-- Distribution
CREATE TABLE history (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT REFERENCES service (id) NOT NULL,
    client_id BIGINT REFERENCES client (id) NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    UNIQUE (service_id, client_id, start_time, end_time)
);