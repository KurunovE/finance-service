CREATE TABLE brokers
(
    id       UUID PRIMARY KEY,
    name     VARCHAR(100) NOT NULL UNIQUE,
    logo_url VARCHAR(255)
);