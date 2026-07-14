CREATE TABLE transactions
(
    id              UUID PRIMARY KEY,
    user_id         UUID                                               NOT NULL,
    category_id     UUID                                               NOT NULL,
    from_account_id UUID,
    to_account_id   UUID,
    liability_id    UUID,
    amount          NUMERIC(15, 2)           DEFAULT 0.00              NOT NULL CHECK (amount > 0),
    currency_id     UUID                                               NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_transactions_category
        FOREIGN KEY (currency_id)
            REFERENCES currencies (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_transactions_currency
        FOREIGN KEY (currency_id)
            REFERENCES currencies (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_from_account
        FOREIGN KEY (from_account_id)
            REFERENCES savings (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_to_account
        FOREIGN KEY (to_account_id)
            REFERENCES savings (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_liability
        FOREIGN KEY (liability_id)
            REFERENCES liabilities (id)
            ON DELETE SET NULL
);