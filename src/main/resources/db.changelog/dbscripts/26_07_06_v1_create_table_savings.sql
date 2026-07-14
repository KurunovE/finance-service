CREATE TABLE savings
(
    account_id          UUID PRIMARY KEY REFERENCES accounts (id) ON DELETE CASCADE,
    interest_rate       NUMERIC(5, 2),
    deposit_term_months INT
);