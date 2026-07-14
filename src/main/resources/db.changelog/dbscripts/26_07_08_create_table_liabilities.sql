CREATE TABLE liabilities
(
    id             UUID PRIMARY KEY,
    user_id        UUID                                               NOT NULL,
    type           VARCHAR(50)                                        NOT NULL CHECK (type in ('CREDIT', 'MORTGAGE', 'DEBT')),
    currency_id    UUID                                               NOT NULL,
    initial_amount NUMERIC(15, 2)           DEFAULT 0.00              NOT NULL CHECK (initial_amount >= 0),
    paid_amount    NUMERIC(15, 2)           DEFAULT 0.00              NOT NULL CHECK (remaining_amount >= 0 AND remaining_amount <= initial_amount),
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at     TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_loans_currency
        FOREIGN KEY (currency_id)
            REFERENCES currencies (id)
            ON DELETE RESTRICT
);