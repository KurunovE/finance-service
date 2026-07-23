CREATE TABLE transactions
(
    id           UUID PRIMARY KEY,
    user_id      UUID NOT NULL,
    category_id  UUID NOT NULL,
    currency_id  UUID NOT NULL,
    amount       DECIMAL(15, 2) DEFAULT 0.00,
    bank         VARCHAR(50),
    description  VARCHAR(255),
    created_date DATE NOT NULL,

    FOREIGN KEY (category_id) REFERENCES categories (id),
    FOREIGN KEY (currency_id) REFERENCES currencies (id)
);