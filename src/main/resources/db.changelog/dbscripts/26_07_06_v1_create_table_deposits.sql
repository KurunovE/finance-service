CREATE TABLE deposits
(
    id            UUID PRIMARY KEY,
    user_id       UUID                     NOT NULL,
    name          VARCHAR(30),
    amount        DECIMAL(15, 2)           NOT NULL,
    currency_id   UUID                     NOT NULL,
    interest_rate DECIMAL(5, 2)            NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_at TIMESTAMP WITH TIME ZONE NOT NULL,

    FOREIGN KEY (currency_id) REFERENCES currencies (id)
);