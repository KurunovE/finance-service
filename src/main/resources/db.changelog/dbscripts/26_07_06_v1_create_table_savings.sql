CREATE TABLE savings
(
    id            UUID PRIMARY KEY,
    user_id       UUID                                               NOT NULL,
    type          VARCHAR(50)                                        NOT NULL CHECK (type in ('CASH', 'SAVINGS_ACCOUNT', 'DEPOSIT')),
    currency_id   UUID                                               NOT NULL,
    amount        NUMERIC(15, 2)           DEFAULT 0.00              NOT NULL CHECK (amount >= 0),
    interest_rate NUMERIC(6, 4)            DEFAULT 0.0000            NOT NULL CHECK (interest_rate >= 0),
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_savings_currency
        FOREIGN KEY (currency_id)
            REFERENCES currencies (id)
            ON DELETE RESTRICT
);