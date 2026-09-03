CREATE TABLE currency_rates
(
    id          UUID PRIMARY KEY,
    currency_id UUID                                               NOT NULL,
    rate        NUMERIC(15, 4)                                     NOT NULL CHECK (rate > 0),
    rate_date   DATE                                               NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    FOREIGN KEY (currency_id) REFERENCES currencies (id) ON DELETE CASCADE,

    CONSTRAINT uq_currency_rate_date UNIQUE (currency_id, rate_date)
);

CREATE INDEX idx_currency_rates_date ON currency_rates (currency_id, rate_date);