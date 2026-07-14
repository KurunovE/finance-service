CREATE TABLE transactions
(
    id              UUID PRIMARY KEY,
    user_id         UUID                                               NOT NULL,
    category_id     UUID                                               NOT NULL,
    currency_id     UUID                                               NOT NULL,
    from_account_id UUID,
    to_account_id   UUID,
    amount          NUMERIC(15, 2)                                     NOT NULL CHECK (amount > 0),
    description     VARCHAR(255),
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_category
        FOREIGN KEY (category_id)
            REFERENCES categories (id) ON DELETE RESTRICT,

    CONSTRAINT fk_currency
        FOREIGN KEY (currency_id)
            REFERENCES currencies (id) ON DELETE RESTRICT,

    CONSTRAINT fk_from_account
        FOREIGN KEY (from_account_id)
            REFERENCES accounts (id) ON DELETE SET NULL,

    CONSTRAINT fk_to_account
        FOREIGN KEY (to_account_id)
            REFERENCES accounts (id) ON DELETE SET NULL
);