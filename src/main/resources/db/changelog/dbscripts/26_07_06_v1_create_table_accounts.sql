CREATE TABLE accounts
(
    id          UUID PRIMARY KEY,
    user_id     UUID           NOT NULL,
    name        VARCHAR(100)   NOT NULL,
    type        VARCHAR(30)    NOT NULL CHECK (type IN ('SAVINGS', 'CASH')),
    balance     DECIMAL(15, 2) NOT NULL  DEFAULT 0.00,
    currency_id UUID           NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_account_currency
        FOREIGN KEY (currency_id)
            REFERENCES currencies (id) ON DELETE RESTRICT
);