-- Создаем валюту (RUB)
INSERT INTO currencies (id, code, name, symbol, is_active)
VALUES ('33333333-3333-3333-3333-333333333333', 'RUB', 'Российский рубль', '₽', true);

-- Создаем две категории для одного пользователя
INSERT INTO categories (id, user_id, name, type, is_deleted)
VALUES ('22222222-2222-2222-2222-222222222221', '11111111-1111-1111-1111-111111111111', 'Зарплата', 'INCOME', false);

INSERT INTO categories (id, user_id, name, type, is_deleted)
VALUES ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'Продукты', 'EXPENSE', false);

-- Создаем транзакции
-- 1. Зарплата (Август, начало)
INSERT INTO transactions (id, user_id, category_id, currency_id, amount, bank, description, created_date, is_deleted)
VALUES ('44444444-4444-4444-4444-444444444441', '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222221', '33333333-3333-3333-3333-333333333333', 100000.00, 'T-Bank', 'ЗП', '2026-08-01', false);

-- 2. Продукты (Август, середина)
INSERT INTO transactions (id, user_id, category_id, currency_id, amount, bank, description, created_date, is_deleted)
VALUES ('44444444-4444-4444-4444-444444444442', '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', 5000.00, 'T-Bank', 'Пятерочка', '2026-08-15', false);

-- 3. Продукты (Сентябрь)
INSERT INTO transactions (id, user_id, category_id, currency_id, amount, bank, description, created_date, is_deleted)
VALUES ('44444444-4444-4444-4444-444444444443', '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', 3000.00, 'Sber', 'Лента', '2026-09-05', false);

-- 4. Мягко удаленная транзакция (Сентябрь) -> НЕ должна попасть в выборку!
INSERT INTO transactions (id, user_id, category_id, currency_id, amount, bank, description, created_date, is_deleted)
VALUES ('44444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', 100.00, 'Sber', 'Удалено', '2026-09-10', true);