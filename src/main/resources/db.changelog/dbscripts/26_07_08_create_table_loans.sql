CREATE TABLE loans
(
    account_id       UUID PRIMARY KEY REFERENCES accounts (id) ON DELETE CASCADE,
    interest_rate    NUMERIC(5, 2)  NOT NULL DEFAULT 0.00,
    initial_amount   NUMERIC(15, 2) NOT NULL CHECK (initial_amount > 0),
    remaining_amount NUMERIC(15, 2) NOT NULL,

    CONSTRAINT chk_remaining_amount
        CHECK (remaining_amount >= 0 AND remaining_amount <= initial_amount)
);