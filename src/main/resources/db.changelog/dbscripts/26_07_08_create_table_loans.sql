CREATE TABLE loans
(
    id            UUID PRIMARY KEY,
    user_id       UUID           NOT NULL,
    name          VARCHAR(30),
    init_amount   DECIMAL(15, 2) NOT NULL,
    currency_id   UUID           NOT NULL,
    interest_rate DECIMAL(5, 2)  NOT NULL,
    paid          DECIMAL(15, 2)           DEFAULT 0.00,
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (currency_id) REFERENCES currencies (id),
);