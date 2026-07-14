CREATE TABLE categories
(
    id      UUID PRIMARY KEY,
    user_id UUID        NOT NULL,
    name    VARCHAR(30) NOT NULL CHECK (TRIM(name) <> ''),
    type    VARCHAR(20) NOT NULL CHECK (type in ('INCOME', 'EXPENSE')),

    CONSTRAINT uq_user_category_name UNIQUE (user_id, name)
);