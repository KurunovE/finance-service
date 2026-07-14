CREATE TABLE portfolios
(
    account_id UUID PRIMARY KEY REFERENCES accounts (id) ON DELETE CASCADE,
    broker_id  UUID NOT NULL,

    CONSTRAINT fk_portfolio_broker
        FOREIGN KEY (broker_id)
            REFERENCES brokers (id) ON DELETE RESTRICT
);