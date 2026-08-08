package com.prorenta.financeservice.factory;

import com.prorenta.financeservice.model.entity.Currency;

import java.util.UUID;

public class CurrencyDataFactory {

    public static final UUID DEFAULT_CURRENCY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final String DEFAULT_CURRENCY_CODE = "RUB";
    public static final String DEFAULT_CURRENCY_NAME = "Рубль";

    public static Currency createDefaultCurrency() {
        return Currency.builder()
                .id(DEFAULT_CURRENCY_ID)
                .code(DEFAULT_CURRENCY_CODE)
                .name(DEFAULT_CURRENCY_NAME)
                .build();
    }
}
