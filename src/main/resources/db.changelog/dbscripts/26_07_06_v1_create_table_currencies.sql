CREATE TABLE currencies
(
    id     UUID PRIMARY KEY,
    code   CHAR(3)     NOT NULL CHECK (TRIM(code) <> ''),
    name   VARCHAR(50) NOT NULL CHECK (TRIM(name) <> ''),
    symbol VARCHAR(5)  NOT NULL CHECK (TRIM(name) <> ''),
);